/** @typedef {{ key: string, type: string, label: string, required?: boolean, default?: unknown, options?: string[], min?: number, max?: number, hint?: string }} Field */

const $ = (sel) => document.querySelector(sel);
const catalogEl = $("#catalog");
const toolTitle = $("#tool-title");
const toolDesc = $("#tool-desc");
const toolForm = $("#tool-form");
const fieldsEl = $("#fields");
const emptyHint = $("#empty-hint");
const jobStatus = $("#job-status");
const btnRun = $("#btn-run");
const btnPreviewRefresh = $("#btn-preview-refresh");
const previewTabs = $("#preview-tabs");
const previewPane = $("#preview-pane");
const previewStatus = $("#preview-status");
const previewHint = $("#preview-hint");
const previewStemBar = $("#preview-stem-bar");
const previewAssetIdPrefix = $("#preview-asset-id-prefix");
const previewStemStatus = $("#preview-stem-status");

const ASSET_ID_RE = /^[a-z0-9][a-z0-9._-]*$/;
/** @type {Record<string, string>} 以预览原始相对路径为键的造型段 */
let variantStemsByRawName = {};
let previewStemTimer = null;

/** @type {{ categories: Array<{ id: string, label: string, tools: Array<{ id: string, label: string, description: string, fields: Field[] }> }> }} */
let catalog = { categories: [] };
/** @type {{ id: string, label: string, description: string, fields: Field[] } | null} */
let currentTool = null;
let eventSource = null;
let running = false;
let previewTimer = null;
let previewLoading = false;
/** @type {Array<{ name: string, target: string, kind: string, text?: string, data_url?: string, message?: string }>} */
let previewItems = [];
let previewActiveIndex = 0;
/** @type {{ assetId: string, baseAssetId: string, idPrefix: string, assetsRoot: string, rawItems: typeof previewItems }} */
let previewContext = {
  assetId: "",
  baseAssetId: "",
  idPrefix: "",
  assetsRoot: "",
  rawItems: [],
};
/** @type {{ suggestions: Record<string, string[]>, last_params: Record<string, string> }} */
let currentHistory = { suggestions: {}, last_params: {} };

const PREVIEWABLE_SUFFIX =
  /\.(json|bbmodel|geo\.json|png|jpe?g|webp|gif|bmp)$/i;

/** @type {ReadonlyArray<{ id: string, label: string }>} */
const PREVIEW_CATEGORIES = [
  { id: "geo", label: "Geo" },
  { id: "material", label: "材质" },
  { id: "animation", label: "动画" },
  { id: "other", label: "其它" },
];

function previewCategoryForItem(item) {
  const n = String(item.name || "")
    .replace(/\\/g, "/")
    .toLowerCase();
  if (n.startsWith("geo/") || n.endsWith(".geo.json")) return "geo";
  if (n.startsWith("animations/") || n.endsWith(".animation.json")) return "animation";
  if (n.startsWith("textures/") || item.kind === "image") return "material";
  return "other";
}

function normPathSlashes(p) {
  return String(p || "").replace(/\\/g, "/").replace(/\/+$/, "");
}

function escapeRegExp(s) {
  return String(s).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function tabBasename(path) {
  const parts = normPathSlashes(path).split("/");
  return parts[parts.length - 1] || path;
}

/** 标签/短路径中去掉与表单 asset-id 重复的前缀 */
function stripAssetIdFromFilename(filename, assetId) {
  if (!assetId || !filename) return filename;
  if (filename === `${assetId}.geo.json`) return "geo.json";
  if (filename === `${assetId}.animation.json`) return "animation.json";
  if (filename === `${assetId}.png`) return "1.png";
  if (filename === `${assetId}_1.png`) return "1.png";
  const numSuffix = filename.match(new RegExp(`^${escapeRegExp(assetId)}_(\\d+)\\.[^.]+$`));
  if (numSuffix) return `${numSuffix[1]}${filename.slice(filename.lastIndexOf("."))}`;
  if (filename.startsWith(`${assetId}.`)) {
    const rest = filename.slice(assetId.length + 1);
    return rest || filename;
  }
  if (filename.startsWith(`${assetId}_`)) {
    const rest = filename.slice(assetId.length + 1);
    return rest || filename;
  }
  return filename;
}

function shortenPreviewRelPath(relPath, index) {
  const parts = normPathSlashes(relPath).split("/").filter(Boolean);
  if (!parts.length) return relPath;
  const composed =
    typeof index === "number"
      ? getComposedAssetIdForIndex(index)
      : previewContext.assetId;
  parts[parts.length - 1] = stripAssetIdFromFilename(
    parts[parts.length - 1],
    composed
  );
  return parts.join("/");
}

function tabDisplayName(item, index) {
  return getTabLabelForIndex(index);
}

function replaceAssetIdInFilename(filename, oldId, newId) {
  if (!filename || !oldId || !newId || oldId === newId) return filename;
  if (filename === `${oldId}.geo.json`) return `${newId}.geo.json`;
  if (filename === `${oldId}.animation.json`) return `${newId}.animation.json`;
  if (filename.startsWith(`${oldId}_`)) return newId + filename.slice(oldId.length);
  if (filename.startsWith(`${oldId}.`)) return newId + filename.slice(oldId.length);
  return filename;
}

function replaceAssetIdInRelPath(relPath, oldId, newId) {
  const parts = normPathSlashes(relPath).split("/").filter(Boolean);
  if (!parts.length) return relPath;
  parts[parts.length - 1] = replaceAssetIdInFilename(
    parts[parts.length - 1],
    oldId,
    newId
  );
  return parts.join("/");
}

function replaceAssetIdInTarget(absPath, root, oldId, newId) {
  if (!absPath) return absPath;
  const rel = pathUnderRoot(absPath, root);
  if (rel !== null) {
    const rootNorm = normPathSlashes(root);
    return `${rootNorm}/${replaceAssetIdInRelPath(rel, oldId, newId)}`;
  }
  return String(absPath).split(oldId).join(newId);
}

function clonePreviewItems(items) {
  return items.map((it) => ({ ...it }));
}

/** 固定 ID：共享贴图键，或左侧 asset-id（当其为完整 id 的前缀时） */
function resolveIdPrefix(fullAssetId, params) {
  const full = String(fullAssetId || "").trim();
  const shared = String(params?.shared || "").trim();
  if (shared && (full === shared || full.startsWith(`${shared}_`))) return shared;
  const formId = String(params?.asset_id || "").trim();
  if (formId && full === formId) {
    if (shared) return shared;
    return formId;
  }
  if (formId && full.startsWith(`${formId}_`)) return formId;
  return "";
}

function parseVariantStem(fullAssetId, idPrefix) {
  const full = String(fullAssetId || "").trim();
  const prefix = String(idPrefix || "").trim();
  if (!prefix) return full;
  if (full === prefix) return "";
  if (full.startsWith(`${prefix}_`)) return full.slice(prefix.length + 1);
  return full;
}

function composeAssetId(idPrefix, variantStem) {
  const prefix = String(idPrefix || "").trim();
  const variant = String(variantStem || "").trim();
  if (!prefix) return variant;
  if (!variant) return prefix;
  return `${prefix}_${variant}`;
}

/** @returns {{ variant: string, suffix: string }} */
function splitFileNameParts(filename, idPrefix, baseAssetId) {
  const b = tabBasename(filename);
  const base = String(baseAssetId || previewContext.baseAssetId || "").trim();
  const prefix = String(idPrefix || "").trim();

  if (b.endsWith(".geo.json")) {
    const stem = b.slice(0, -".geo.json".length);
    if (prefix && stem.startsWith(`${prefix}_`)) {
      return { variant: stem.slice(prefix.length + 1), suffix: "geo.json" };
    }
    if (prefix && stem === prefix) return { variant: "", suffix: "geo.json" };
    return { variant: stem, suffix: "geo.json" };
  }
  if (b.endsWith(".animation.json")) {
    const stem = b.slice(0, -".animation.json".length);
    if (prefix && stem.startsWith(`${prefix}_`)) {
      return { variant: stem.slice(prefix.length + 1), suffix: "animation.json" };
    }
    if (prefix && stem === prefix) return { variant: "", suffix: "animation.json" };
    return { variant: stem, suffix: "animation.json" };
  }

  // 标准导出贴图：<baseAssetId>_1.png … _N.png（槽位属于后缀；造型段取自 base 相对 ID 前缀）
  if (base) {
    const slotOnBase = b.match(
      new RegExp(`^${escapeRegExp(base)}_(\\d+)(\\.[^.]+)$`)
    );
    if (slotOnBase) {
      let variant = "";
      if (prefix && base.startsWith(`${prefix}_`)) {
        variant = base.slice(prefix.length + 1);
      } else if (prefix && base === prefix) {
        variant = "";
      }
      return { variant, suffix: `${slotOnBase[1]}${slotOnBase[2]}` };
    }
  }

  if (prefix && base && prefix !== base && b.startsWith(`${prefix}_`)) {
    const rest = b.slice(prefix.length + 1);
    const slotAfterVariant = rest.match(/^(.+)_(\d+)(\.[^.]+)$/);
    if (slotAfterVariant) {
      return {
        variant: slotAfterVariant[1],
        suffix: `${slotAfterVariant[2]}${slotAfterVariant[3]}`,
      };
    }
  }

  if (prefix && b.startsWith(`${prefix}_`)) {
    const rest = b.slice(prefix.length + 1);
    const slotNum = rest.match(/^(.+)_(\d+)(\.[^.]+)$/);
    if (slotNum) {
      return { variant: slotNum[1], suffix: `${slotNum[2]}${slotNum[3]}` };
    }
    const dot = rest.lastIndexOf(".");
    if (dot > 0) {
      return { variant: rest.slice(0, dot), suffix: rest.slice(dot + 1) };
    }
    return { variant: rest, suffix: "" };
  }

  const dot = b.lastIndexOf(".");
  if (dot > 0) return { variant: b.slice(0, dot), suffix: b.slice(dot + 1) };
  return { variant: b, suffix: "" };
}

function getFileSuffixForIndex(index) {
  const raw = previewContext.rawItems[index];
  if (!raw) return "";
  return splitFileNameParts(
    tabBasename(raw.name),
    previewContext.idPrefix,
    previewContext.baseAssetId
  ).suffix;
}

function suffixJoiner(suffix) {
  if (/^\d+\.[^.]+$/i.test(suffix)) return "_";
  if (suffix === "geo.json" || suffix === "animation.json") return ".";
  return suffix ? "_" : "";
}

/** 与标签、造型段输入框共用的显示名 */
function getTabLabelForIndex(index) {
  const raw = previewContext.rawItems[index];
  if (!raw) return "";
  const stem = getVariantStemForIndex(index);
  const def = getDefaultVariantStemForRaw(raw);
  const custom = stem !== def;
  const suffix = getFileSuffixForIndex(index);
  const star = custom ? " *" : "";
  if (/^\d+\.[^.]+$/i.test(suffix)) {
    if (stem) return `${stem} · ${suffix}${star}`;
    return suffix;
  }
  if (stem) return stem + star;
  return suffix;
}

function composeFileNameFromParts(idPrefix, variant, suffix) {
  const composed = composeAssetId(idPrefix, variant);
  if (suffix === "geo.json") return `${composed}.geo.json`;
  if (suffix === "animation.json") return `${composed}.animation.json`;
  if (/^\d+\.[^.]+$/i.test(suffix)) return `${composed}_${suffix}`;
  if (suffix) return `${composed}.${suffix}`;
  return composed;
}

function replaceFileNameInRelPath(relPath, idPrefix, variant, baseAssetId) {
  const parts = normPathSlashes(relPath).split("/").filter(Boolean);
  if (!parts.length) return relPath;
  const { suffix } = splitFileNameParts(
    parts[parts.length - 1],
    idPrefix,
    baseAssetId
  );
  parts[parts.length - 1] = composeFileNameFromParts(idPrefix, variant, suffix);
  return parts.join("/");
}

function getRawItemKey(item) {
  return normPathSlashes(item.name);
}

function getDefaultVariantStemForRaw(rawItem) {
  return splitFileNameParts(
    tabBasename(rawItem.name),
    previewContext.idPrefix,
    previewContext.baseAssetId
  ).variant;
}

function getVariantStemForIndex(index) {
  const raw = previewContext.rawItems[index];
  if (!raw) return "";
  const key = getRawItemKey(raw);
  if (Object.hasOwn(variantStemsByRawName, key)) {
    return variantStemsByRawName[key];
  }
  return getDefaultVariantStemForRaw(raw);
}

function setVariantStemForIndex(index, variant) {
  const raw = previewContext.rawItems[index];
  if (!raw) return;
  variantStemsByRawName[getRawItemKey(raw)] = variant;
}

function getComposedAssetIdForIndex(index) {
  const stem = getVariantStemForIndex(index);
  const composed = composeAssetId(previewContext.idPrefix, stem);
  return composed || previewContext.baseAssetId;
}

function findGeoRawIndex() {
  return previewContext.rawItems.findIndex(
    (it) => previewCategoryForItem(it) === "geo"
  );
}

function getPrimaryExportAssetId() {
  const gi = findGeoRawIndex();
  if (gi >= 0) return getComposedAssetIdForIndex(gi);
  if (previewContext.rawItems.length) return getComposedAssetIdForIndex(0);
  return previewContext.baseAssetId;
}

function updatePreviewIdPrefixDisplay() {
  const prefix = previewContext.idPrefix || "";
  if (previewAssetIdPrefix) {
    previewAssetIdPrefix.textContent = prefix || "（未固定 ID，按文件解析）";
  }
}

function initVariantStemsFromRaw(preserveUser = true) {
  const prev = preserveUser ? { ...variantStemsByRawName } : {};
  variantStemsByRawName = {};
  for (const raw of previewContext.rawItems) {
    const key = getRawItemKey(raw);
    const { suffix } = splitFileNameParts(
      tabBasename(raw.name),
      previewContext.idPrefix,
      previewContext.baseAssetId
    );
    const def = getDefaultVariantStemForRaw(raw);
    let stem = def;
    if (Object.hasOwn(prev, key)) {
      const trial = composeFileNameFromParts(
        previewContext.idPrefix,
        prev[key],
        suffix
      );
      if (trial === tabBasename(raw.name)) stem = prev[key];
    }
    variantStemsByRawName[key] = stem;
  }
}

function rebuildPreviewItemsFromStems() {
  const oldId = previewContext.baseAssetId;
  if (!previewContext.rawItems.length) return;
  previewItems = previewContext.rawItems.map((item, index) => {
    const variant = getVariantStemForIndex(index);
    const name = replaceFileNameInRelPath(
      item.name,
      previewContext.idPrefix,
      variant,
      previewContext.baseAssetId
    );
    const newId = composeAssetId(previewContext.idPrefix, variant) || oldId;
    return {
      ...item,
      name,
      target: replaceAssetIdInTarget(
        item.target,
        previewContext.assetsRoot,
        oldId,
        newId
      ),
    };
  });
  previewContext.assetId = getComposedAssetIdForIndex(previewActiveIndex);
}

function validateVariantStem(variant, index) {
  const composed = composeAssetId(previewContext.idPrefix, variant);
  if (!composed && !variant) return { ok: true, composed: previewContext.baseAssetId };
  if (!ASSET_ID_RE.test(composed)) {
    return { ok: false, message: "组合后须为合法 resource id" };
  }
  return { ok: true, composed };
}

function updatePreviewStemStatusSummary() {
  if (!previewStemStatus || !isExportBbmodelTool()) return;
  let custom = 0;
  let invalid = 0;
  previewContext.rawItems.forEach((raw, index) => {
    const key = getRawItemKey(raw);
    if (
      Object.hasOwn(variantStemsByRawName, key) &&
      variantStemsByRawName[key] !== getDefaultVariantStemForRaw(raw)
    ) {
      custom += 1;
    }
    if (!validateVariantStem(getVariantStemForIndex(index), index).ok) invalid += 1;
  });
  if (invalid > 0) {
    previewStemStatus.textContent = `${invalid} 个文件造型段不合法`;
    previewStemStatus.className = "preview-stem-status err";
    return;
  }
  previewStemStatus.textContent = custom
    ? `已自定义 ${custom} 个文件的造型段`
    : "选中文件后在下方编辑造型段";
  previewStemStatus.className = "preview-stem-status muted";
}

function onPerFileVariantInput(index, value) {
  setVariantStemForIndex(index, value);
  rebuildPreviewItemsFromStems();
  updatePreviewStemStatusSummary();
  renderPreviewTabs();
  renderPreviewPane();
}

function setPreviewStemBarVisible(show) {
  if (previewStemBar) previewStemBar.hidden = !show;
}

function resetPreviewStemFromData(assetId, params, preserveUser = false) {
  const full = String(assetId || "").trim();
  const p = params || collectParams();
  previewContext.idPrefix = resolveIdPrefix(full, p);
  initVariantStemsFromRaw(preserveUser);
  rebuildPreviewItemsFromStems();
  updatePreviewIdPrefixDisplay();
  updatePreviewStemStatusSummary();
}

function collectAssetRenames() {
  const root = normPathSlashes(previewContext.assetsRoot);
  const base = previewContext.baseAssetId;
  const primary = getPrimaryExportAssetId();
  if (!root || !base) return [];
  /** @type {Array<{ from: string, to: string }>} */
  const renames = [];
  for (let i = 0; i < previewContext.rawItems.length; i++) {
    const exportedRel = replaceAssetIdInRelPath(
      previewContext.rawItems[i].name,
      base,
      primary
    );
    const desiredRel = normPathSlashes(previewItems[i]?.name || exportedRel);
    if (exportedRel !== desiredRel) {
      renames.push({
        from: `${root}/${exportedRel}`,
        to: `${root}/${desiredRel}`,
      });
    }
  }
  return renames;
}

function pathUnderRoot(fullPath, rootPath) {
  const f = normPathSlashes(fullPath).toLowerCase();
  const r = normPathSlashes(rootPath).toLowerCase();
  if (!r) return null;
  if (f === r) return "";
  if (f.startsWith(r + "/")) return normPathSlashes(fullPath).slice(rootPath.length + 1);
  return null;
}

/** 写入路径：相对「导出目标」目录，且文件名省略 asset-id */
function formatExportPathDisplay(item, index) {
  const root = previewContext.assetsRoot;
  let rel = normPathSlashes(item.name);
  if (!rel && item.target) {
    const fromTarget = pathUnderRoot(item.target, root);
    rel = fromTarget !== null ? fromTarget : normPathSlashes(item.target);
  }
  return shortenPreviewRelPath(rel, index);
}

function hasNativePicker() {
  return !!(window.pywebview && window.pywebview.api);
}

async function pickFile() {
  if (hasNativePicker()) {
    try {
      const paths = await window.pywebview.api.pick_file();
      if (paths && paths.length) return paths[0];
    } catch (e) {
      console.warn("pick_file", e);
    }
  }
  return null;
}

async function pickDirectory() {
  if (hasNativePicker()) {
    try {
      const paths = await window.pywebview.api.pick_directory();
      if (paths && paths.length) return paths[0];
    } catch (e) {
      console.warn("pick_directory", e);
    }
  }
  return null;
}

function setStatus(kind, text) {
  if (!jobStatus) return;
  jobStatus.className = `badge ${kind}`;
  jobStatus.textContent = text;
}

/** 子进程日志：浏览器控制台 + 服务端终端（runner 已写 stdout） */
function logToConsole(text) {
  if (text == null || text === "") return;
  const lines = String(text).split(/\n/);
  for (const line of lines) {
    if (line.length) console.log(line);
  }
}

function renderCatalog() {
  catalogEl.innerHTML = "";
  for (const cat of catalog.categories) {
    const block = document.createElement("div");
    block.className = "cat-block";
    const title = document.createElement("div");
    title.className = "cat-title";
    title.textContent = cat.label;
    block.appendChild(title);
    for (const tool of cat.tools) {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "tool-btn";
      btn.dataset.toolId = tool.id;
      btn.textContent = tool.label;
      btn.addEventListener("click", () => selectTool(tool.id));
      block.appendChild(btn);
    }
    catalogEl.appendChild(block);
  }
}

async function fetchToolHistory(toolId) {
  try {
    const r = await fetch(`/api/form-history/${encodeURIComponent(toolId)}`);
    if (r.ok) return await r.json();
  } catch (e) {
    console.warn("form-history", e);
  }
  return { suggestions: {}, last_params: {} };
}

async function rememberFields(params) {
  if (!currentTool || !params) return;
  try {
    await fetch("/api/form-history/remember", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ tool_id: currentTool.id, params }),
    });
    currentHistory = await fetchToolHistory(currentTool.id);
    attachDatalistsToForm();
  } catch (e) {
    console.warn("remember", e);
  }
}

async function selectTool(toolId) {
  let found = null;
  for (const cat of catalog.categories) {
    const t = cat.tools.find((x) => x.id === toolId);
    if (t) {
      found = t;
      break;
    }
  }
  if (!found) return;

  currentTool = found;
  document.querySelectorAll(".tool-btn").forEach((b) => {
    b.classList.toggle("active", b.dataset.toolId === toolId);
  });

  toolTitle.textContent = found.label;
  toolDesc.textContent = found.description || "";
  emptyHint.hidden = true;
  toolForm.hidden = false;
  currentHistory = await fetchToolHistory(toolId);
  renderFields(found.fields || [], currentHistory);
  updateToolActions();
  clearPreview();
  schedulePreview(80);
}

function isExportBbmodelTool() {
  return currentTool?.id === "export_bbmodel";
}

function updateToolActions() {
  if (!btnRun || !btnPreviewRefresh) return;
  if (isExportBbmodelTool()) {
    btnRun.textContent = "保存到 assets";
    btnPreviewRefresh.hidden = false;
    setPreviewStemBarVisible(true);
    updatePreviewIdPrefixDisplay();
    if (previewHint) {
      previewHint.textContent =
        "每个预览文件可单独编辑造型段（ID 与 geo.json / _N 等后缀之间）；确认后保存到 assets";
    }
  } else {
    btnRun.textContent = "运行";
    btnPreviewRefresh.hidden = true;
    setPreviewStemBarVisible(false);
    if (previewHint) {
      previewHint.textContent = "选择 JSON / 图片路径后将自动预览";
    }
  }
}

function setPreviewStatus(kind, text) {
  if (!previewStatus) return;
  previewStatus.className = `badge ${kind}`;
  previewStatus.textContent = text;
}

function setPreviewPaneMessage(text, kind = "idle") {
  if (!previewPane) return;
  previewPane.innerHTML = "";
  const p = document.createElement("p");
  p.className = `preview-pane-msg preview-pane-msg--${kind}`;
  p.textContent = text;
  previewPane.appendChild(p);
}

function clearPreview() {
  previewItems = [];
  previewActiveIndex = 0;
  previewContext = {
    assetId: "",
    baseAssetId: "",
    idPrefix: "",
    assetsRoot: "",
    rawItems: [],
  };
  variantStemsByRawName = {};
  if (previewTabs) previewTabs.innerHTML = "";
  setPreviewPaneMessage("填写路径后将自动预览；点标签切换 geo / 贴图。", "idle");
  setPreviewStatus("idle", "—");
  if (!isExportBbmodelTool()) setPreviewStemBarVisible(false);
}

function schedulePreview(delayMs = 450) {
  if (previewTimer) clearTimeout(previewTimer);
  previewTimer = setTimeout(() => {
    previewTimer = null;
    refreshPreview();
  }, delayMs);
}

function shouldAutoPreview() {
  if (!currentTool) return false;
  if (isExportBbmodelTool()) {
    const p = collectParams().path;
    return typeof p === "string" && p.trim().length > 0;
  }
  for (const field of currentTool.fields || []) {
    if (field.type !== "file") continue;
    const wrap = fieldsEl.querySelector(`[data-key="${field.key}"]`);
    const el = wrap?.querySelector("input[type='text']");
    const v = el?.value?.trim();
    if (v && PREVIEWABLE_SUFFIX.test(v)) return true;
  }
  return false;
}

async function refreshPreview() {
  if (!currentTool || previewLoading) return;
  if (!shouldAutoPreview()) {
    clearPreview();
    return;
  }

  previewLoading = true;
  setPreviewStatus("running", "加载中…");
  const params = collectParams();

  let res;
  try {
    res = await fetch("/api/preview", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ tool_id: currentTool.id, params }),
    });
  } catch (e) {
    previewLoading = false;
    clearPreview();
    setPreviewStatus("err", "失败");
    setPreviewPaneMessage(`预览请求失败: ${e}`, "err");
    return;
  }

  previewLoading = false;
  if (!res.ok) {
    let msg = res.statusText;
    try {
      const body = await res.json();
      const d = body.detail;
      if (typeof d === "object" && d.message) msg = d.message;
      else if (typeof d === "string") msg = d;
    } catch (_) {
      /* ignore */
    }
    setPreviewStatus("err", "无法预览");
    if (previewTabs) previewTabs.innerHTML = "";
    previewItems = [];
    setPreviewPaneMessage(msg, "err");
    return;
  }

  const data = await res.json();
  previewItems = data.items || [];
  const resolvedId = String(data.asset_id || params.asset_id || "").trim();
  previewContext = {
    assetId: resolvedId,
    baseAssetId: resolvedId,
    idPrefix: resolveIdPrefix(resolvedId, params),
    assetsRoot: normPathSlashes(data.assets_root || params.assets_root || ""),
    rawItems: clonePreviewItems(previewItems),
  };
  if (isExportBbmodelTool()) {
    setPreviewStemBarVisible(true);
    resetPreviewStemFromData(resolvedId, params, true);
  } else {
    variantStemsByRawName = {};
  }
  if (!previewItems.length) {
    setPreviewStatus("idle", "无内容");
    if (previewTabs) previewTabs.innerHTML = "";
    previewItems = [];
    setPreviewPaneMessage(
      data.messages?.join(" ") || "未生成可预览的文件",
      "idle"
    );
    return;
  }

  const preferOrder = ["geo", "material", "animation", "other"];
  let firstIndex = 0;
  for (const cat of preferOrder) {
    const idx = previewItems.findIndex((it) => previewCategoryForItem(it) === cat);
    if (idx >= 0) {
      firstIndex = idx;
      break;
    }
  }
  previewActiveIndex = firstIndex;
  renderPreviewTabs();
  renderPreviewPane();
  let label = `${previewItems.length} 个文件`;
  const stemLabel = getPrimaryExportAssetId() || data.asset_id;
  if (data.mode === "export_bbmodel" && stemLabel) {
    label += ` · geo→${stemLabel}`;
  }
  if (data.assets_root) {
    label += " → 自定义目录";
  }
  setPreviewStatus("ok", label);
}

function renderPreviewTabs() {
  if (!previewTabs) return;
  previewTabs.innerHTML = "";

  /** @type {Map<string, Array<{ item: typeof previewItems[0], index: number }>>} */
  const grouped = new Map(PREVIEW_CATEGORIES.map((c) => [c.id, []]));
  previewItems.forEach((item, index) => {
    const cat = previewCategoryForItem(item);
    const bucket = grouped.get(cat) || grouped.get("other");
    bucket.push({ item, index });
  });

  for (const { id, label } of PREVIEW_CATEGORIES) {
    const entries = grouped.get(id);
    if (!entries?.length) continue;

    const section = document.createElement("div");
    section.className = "preview-tab-group";
    section.setAttribute("role", "group");
    section.setAttribute("aria-label", label);

    const groupLabel = document.createElement("div");
    groupLabel.className = "preview-tab-group-label";
    groupLabel.textContent = `${label} (${entries.length})`;
    section.appendChild(groupLabel);

    const row = document.createElement("div");
    row.className = "preview-tab-row";
    row.setAttribute("role", "tablist");

    for (const { item, index } of entries) {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className =
        "preview-tab" + (index === previewActiveIndex ? " active" : "");
      btn.title = item.target || item.name;
      const suffix = getFileSuffixForIndex(index);
      btn.textContent = getTabLabelForIndex(index);
      btn.dataset.suffix = suffix;
      btn.dataset.variant = getVariantStemForIndex(index);
      btn.setAttribute("role", "tab");
      btn.setAttribute(
        "aria-selected",
        index === previewActiveIndex ? "true" : "false"
      );
      btn.addEventListener("click", () => {
        previewActiveIndex = index;
        renderPreviewTabs();
        renderPreviewPane();
      });
      row.appendChild(btn);
    }

    section.appendChild(row);
    previewTabs.appendChild(section);
  }
}

function renderPreviewPane() {
  if (!previewPane) return;
  const item = previewItems[previewActiveIndex];
  const index = previewActiveIndex;
  if (!item) {
    previewPane.innerHTML = "";
    return;
  }
  previewPane.innerHTML = "";

  if (isExportBbmodelTool() && previewContext.rawItems[index]) {
    const rawName = previewContext.rawItems[index].name;
    const suffix = getFileSuffixForIndex(index);
    const stemRow = document.createElement("div");
    stemRow.className = "preview-file-stem";

    const tabLabel = getTabLabelForIndex(index);
    const label = document.createElement("span");
    label.className = "preview-file-stem-label";
    label.textContent = `造型段 · ${tabLabel}`;
    stemRow.appendChild(label);

    const compose = document.createElement("div");
    compose.className = "preview-stem-compose";

    const prefix = previewContext.idPrefix || "";
    if (prefix) {
      const preEl = document.createElement("span");
      preEl.className = "preview-asset-id-prefix";
      preEl.textContent = prefix;
      compose.appendChild(preEl);
      const sep = document.createElement("span");
      sep.className = "preview-stem-sep muted";
      sep.textContent = "_";
      compose.appendChild(sep);
    }

    const input = document.createElement("input");
    input.type = "text";
    input.className = "preview-export-stem";
    input.autocomplete = "off";
    input.spellcheck = false;
    input.placeholder = prefix ? "如 shape_225" : "完整 asset-id 中段";
    input.value = getVariantStemForIndex(index);
    input.addEventListener("input", () => {
      onPerFileVariantInput(index, input.value);
      const v = validateVariantStem(input.value, index);
      input.classList.toggle("invalid", !v.ok);
    });
    compose.appendChild(input);

    if (suffix) {
      const sep2 = document.createElement("span");
      sep2.className = "preview-stem-sep muted";
      sep2.textContent = suffixJoiner(suffix);
      compose.appendChild(sep2);
      const suf = document.createElement("span");
      suf.className = "preview-file-suffix muted";
      suf.textContent = suffix;
      compose.appendChild(suf);
    }

    stemRow.appendChild(compose);

    const hint = document.createElement("span");
    hint.className = "preview-file-stem-hint muted";
    const v = validateVariantStem(input.value, index);
    hint.textContent = v.ok
      ? `→ ${tabBasename(item.name)}`
      : v.message || "不合法";
    hint.classList.toggle("err", !v.ok);
    stemRow.appendChild(hint);

    previewPane.appendChild(stemRow);
  }

  const exportRel = formatExportPathDisplay(item, index);
  if (exportRel) {
    const target = document.createElement("p");
    target.className = "preview-target";
    target.textContent = `写入: ${exportRel}`;
    target.title = item.target || item.name;
    previewPane.appendChild(target);
  }
  if (item.kind === "image" && item.data_url) {
    const wrap = document.createElement("div");
    wrap.className = "preview-image-wrap";
    const img = document.createElement("img");
    img.src = item.data_url;
    img.alt = tabDisplayName(item, index);
    wrap.appendChild(img);
    previewPane.appendChild(wrap);
  } else if (item.kind === "json" && item.text != null) {
    const pre = document.createElement("pre");
    pre.className = "preview-json";
    pre.textContent = item.text;
    previewPane.appendChild(pre);
  } else {
    const p = document.createElement("p");
    p.className = "preview-binary";
    p.textContent = item.message || "无法在此预览该文件类型";
    previewPane.appendChild(p);
  }
}

function bindPreviewListeners() {
  if (!fieldsEl) return;
  fieldsEl.querySelectorAll("input[type='text'], select").forEach((el) => {
    el.addEventListener("input", () => {
      if (el.id === "f-asset_id" || el.id === "f-shared") {
        applyFormAssetIdToPreviewContext();
      }
      schedulePreview();
    });
    el.addEventListener("change", () => {
      if (el.id === "f-asset_id" || el.id === "f-shared") {
        applyFormAssetIdToPreviewContext();
      }
      schedulePreview();
    });
  });
  fieldsEl.querySelectorAll('input[type="checkbox"]').forEach((el) => {
    el.addEventListener("change", () => schedulePreview());
  });
}

function fieldInitialValue(field, history) {
  const last = history?.last_params?.[field.key];
  if (last !== undefined && last !== null && String(last).length > 0) {
    return String(last);
  }
  return defaultValue(field);
}

function addHistorySelect(row, field, input, items) {
  if (!items.length) return;
  const sel = document.createElement("select");
  sel.className = "history-select";
  sel.title = "选择历史记录";
  const ph = document.createElement("option");
  ph.value = "";
  ph.textContent = `历史 ${items.length}`;
  sel.appendChild(ph);
  for (const v of items) {
    const o = document.createElement("option");
    o.value = v;
    const short =
      v.length > 42 ? `…${v.slice(-39)}` : v;
    o.textContent = short;
    o.title = v;
    sel.appendChild(o);
  }
  sel.addEventListener("change", () => {
    if (!sel.value) return;
    input.value = sel.value;
    sel.value = "";
    schedulePreview(200);
  });
  row.appendChild(sel);
}

function attachDatalistsToForm() {
  if (!currentTool) return;
  for (const field of currentTool.fields || []) {
    if (!["file", "directory", "text"].includes(field.type)) continue;
    const input = fieldsEl.querySelector(`#f-${CSS.escape(field.key)}`);
    if (!input || input.tagName !== "INPUT" || input.type !== "text") continue;
    const listId = `dl-${currentTool.id}-${field.key}`;
    let dl = document.getElementById(listId);
    if (!dl) {
      dl = document.createElement("datalist");
      dl.id = listId;
      fieldsEl.appendChild(dl);
    }
    dl.innerHTML = "";
    const items = currentHistory.suggestions?.[field.key] || [];
    for (const v of items) {
      const opt = document.createElement("option");
      opt.value = v;
      dl.appendChild(opt);
    }
    input.setAttribute("list", listId);
    const row = input.closest(".field-row");
    const existing = row?.querySelector(".history-select");
    if (existing) existing.remove();
    if (items.length && row) {
      addHistorySelect(row, field, input, items);
    }
  }
}

function defaultValue(field) {
  if (field.type === "bool") return !!field.default;
  if (field.type === "number") {
    return field.default !== undefined && field.default !== null
      ? String(field.default)
      : "";
  }
  return field.default != null ? String(field.default) : "";
}

function renderFields(fields, history = { suggestions: {}, last_params: {} }) {
  fieldsEl.innerHTML = "";
  for (const field of fields) {
    const wrap = document.createElement("div");
    wrap.className = "field";
    wrap.dataset.key = field.key;

    if (field.type === "bool") {
      wrap.className = "field field-check";
      const id = `f-${field.key}`;
      const cb = document.createElement("input");
      cb.type = "checkbox";
      cb.id = id;
      const lastBool = history?.last_params?.[field.key];
      if (lastBool !== undefined && lastBool !== null) {
        cb.checked =
          typeof lastBool === "boolean"
            ? lastBool
            : ["1", "true", "yes", "on"].includes(String(lastBool).toLowerCase());
      } else {
        cb.checked = defaultValue(field);
      }
      const lab = document.createElement("label");
      lab.htmlFor = id;
      lab.textContent = field.label;
      wrap.appendChild(cb);
      wrap.appendChild(lab);
    } else {
      const lab = document.createElement("label");
      lab.textContent = field.label;
      wrap.appendChild(lab);

      const row = document.createElement("div");
      row.className = "field-row";

      let input;
      let browseBtn = null;
      if (field.type === "file" || field.type === "directory") {
        browseBtn = document.createElement("button");
        browseBtn.type = "button";
        browseBtn.className = "btn ghost small";
        browseBtn.textContent = hasNativePicker() ? "浏览…" : "路径";
        browseBtn.title = hasNativePicker()
          ? "系统文件对话框"
          : "请手动粘贴本机绝对路径";
        row.appendChild(browseBtn);
      }

      if (field.type === "select") {
        input = document.createElement("select");
        for (const opt of field.options || []) {
          const o = document.createElement("option");
          o.value = opt;
          o.textContent = opt || "（无）";
          input.appendChild(o);
        }
        const selVal = fieldInitialValue(field, history);
        input.value =
          (field.options || []).includes(selVal) ? selVal : defaultValue(field);
      } else if (field.type === "number") {
        input = document.createElement("input");
        input.type = "number";
        input.min = field.min ?? 0;
        input.max = field.max ?? 99;
        const lastNum = history?.last_params?.[field.key];
        input.value =
          lastNum !== undefined && lastNum !== null && String(lastNum) !== ""
            ? String(lastNum)
            : defaultValue(field);
      } else {
        input = document.createElement("input");
        input.type = "text";
        input.value = fieldInitialValue(field, history);
        if (field.required) input.required = true;
        input.autocomplete = "off";
        const listId = `dl-${currentTool?.id || "tool"}-${field.key}`;
        const dl = document.createElement("datalist");
        dl.id = listId;
        const items = history?.suggestions?.[field.key] || [];
        for (const v of items) {
          const opt = document.createElement("option");
          opt.value = v;
          dl.appendChild(opt);
        }
        input.setAttribute("list", listId);
        row.appendChild(dl);
      }
      input.name = field.key;
      input.id = `f-${field.key}`;
      row.appendChild(input);

      if (
        ["file", "directory", "text"].includes(field.type) &&
        (history?.suggestions?.[field.key] || []).length > 0
      ) {
        addHistorySelect(row, field, input, history.suggestions[field.key]);
      }

      if (browseBtn) {
        browseBtn.addEventListener("click", async () => {
          const p =
            field.type === "file" ? await pickFile() : await pickDirectory();
          if (p) {
            input.value = p;
            await rememberFields({ [field.key]: p });
            schedulePreview(120);
          } else if (!hasNativePicker()) input.focus();
        });
      }

      wrap.appendChild(row);
      if (["file", "directory", "text"].includes(field.type)) {
        const n = (history?.suggestions?.[field.key] || []).length;
        if (n > 0) {
          const hi = document.createElement("p");
          hi.className = "field-hint-history";
          hi.textContent = `输入框 ▼ 或自动补全，最多保留 ${n} 条历史（上限 100）`;
          wrap.appendChild(hi);
        }
      }
      if (field.hint) {
        const h = document.createElement("p");
        h.className = "hint";
        h.textContent = field.hint;
        wrap.appendChild(h);
      }
    }
    fieldsEl.appendChild(wrap);
  }
  attachDatalistsToForm();
  bindPreviewListeners();
}

/** 根据左侧 asset-id / 共享贴图更新预览区 ID 前缀（不写回表单） */
function applyFormAssetIdToPreviewContext() {
  if (!isExportBbmodelTool() || !previewContext.rawItems.length) return;
  const params = collectParams();
  const full = previewContext.baseAssetId;
  previewContext.idPrefix = resolveIdPrefix(full, params);
  initVariantStemsFromRaw(true);
  rebuildPreviewItemsFromStems();
  updatePreviewIdPrefixDisplay();
  updatePreviewStemStatusSummary();
  renderPreviewTabs();
  renderPreviewPane();
}

function collectParams() {
  /** @type {Record<string, unknown>} */
  const params = {};
  if (!currentTool) return params;
  for (const field of currentTool.fields || []) {
    const wrap = fieldsEl.querySelector(`[data-key="${field.key}"]`);
    if (!wrap) continue;
    if (field.type === "bool") {
      const cb = wrap.querySelector('input[type="checkbox"]');
      params[field.key] = cb ? cb.checked : false;
    } else {
      const el = wrap.querySelector("input, select");
      if (!el) continue;
      if (field.type === "number") {
        params[field.key] = el.value === "" ? field.default : Number(el.value);
      } else {
        params[field.key] = el.value;
      }
    }
  }
  return params;
}

function closeEventSource() {
  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }
}

function subscribeJob(jobId) {
  closeEventSource();
  eventSource = new EventSource(`/api/jobs/${jobId}/events`);
  eventSource.onmessage = (ev) => {
    try {
      const data = JSON.parse(ev.data);
      if (data.type === "line") {
        logToConsole(data.text);
      } else if (data.type === "done") {
        closeEventSource();
        running = false;
        btnRun.disabled = false;
        if (data.error) {
          setStatus("err", "失败");
        } else if (data.exit_code === 0) {
          setStatus("ok", isExportBbmodelTool() ? "已保存" : "完成");
          rememberFields(collectParams());
          if (isExportBbmodelTool()) schedulePreview(200);
        } else {
          setStatus("err", `退出码 ${data.exit_code}`);
        }
      }
    } catch (e) {
      console.error(e);
    }
  };
  eventSource.onerror = () => {
    closeEventSource();
    if (running) {
      fetch(`/api/jobs/${jobId}`)
        .then((r) => r.json())
        .then((j) => {
          for (const line of j.lines || []) logToConsole(line);
          running = false;
          btnRun.disabled = false;
          if (j.exit_code === 0) {
            setStatus("ok", isExportBbmodelTool() ? "已保存" : "完成");
            rememberFields(collectParams());
            if (isExportBbmodelTool()) schedulePreview(200);
          } else setStatus("err", `退出码 ${j.exit_code}`);
        })
        .catch(() => {
          running = false;
          btnRun.disabled = false;
          setStatus("err", "连接中断");
        });
    }
  };
}

async function onSubmit(e) {
  e.preventDefault();
  if (!currentTool || running) return;

  running = true;
  btnRun.disabled = true;
  setStatus("running", isExportBbmodelTool() ? "导出中…" : "运行中…");

  const params = collectParams();
  if (isExportBbmodelTool()) {
    params.dry_run = false;
    for (let i = 0; i < previewContext.rawItems.length; i++) {
      if (!validateVariantStem(getVariantStemForIndex(i), i).ok) {
        setStatus("err", "存在不合法的造型段，请检查各文件");
        running = false;
        btnRun.disabled = false;
        updatePreviewStemStatusSummary();
        renderPreviewPane();
        return;
      }
    }
    const primary = getPrimaryExportAssetId();
    if (primary) params.asset_id = primary;
    const renames = collectAssetRenames();
    if (renames.length) params._asset_renames = renames;
  }
  let res;
  try {
    res = await fetch("/api/run", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ tool_id: currentTool.id, params }),
    });
  } catch (err) {
    console.error("[请求失败]", err);
    running = false;
    btnRun.disabled = false;
    setStatus("err", "失败");
    return;
  }

  if (!res.ok) {
    let msg = res.statusText;
    try {
      const body = await res.json();
      const d = body.detail;
      if (typeof d === "object" && d.message) msg = d.message;
      else if (typeof d === "string") msg = d;
    } catch (_) {
      /* ignore */
    }
    console.error("[错误]", msg);
    running = false;
    btnRun.disabled = false;
    setStatus("err", "失败");
    return;
  }

  const { job_id } = await res.json();
  subscribeJob(job_id);
}

async function init() {
  const infoRes = await fetch("/api/info");
  const info = await infoRes.json();
  if (hasNativePicker()) {
    document.querySelector(".brand .sub").textContent =
      "开发工具 · 内嵌 WebView";
  }

  const catRes = await fetch("/api/catalog");
  catalog = await catRes.json();
  renderCatalog();

  toolForm.addEventListener("submit", onSubmit);
  if (btnPreviewRefresh) {
    btnPreviewRefresh.addEventListener("click", () => refreshPreview());
  }
}

init().catch((e) => {
  console.error("[启动失败]", e);
  setStatus("err", "失败");
});
