/** @typedef {{ key: string, type: string, label: string, required?: boolean, default?: unknown, options?: string[], min?: number, max?: number, hint?: string }} Field */

const $ = (sel) => document.querySelector(sel);
const catalogEl = $("#catalog");
const toolTitle = $("#tool-title");
const toolDesc = $("#tool-desc");
const toolForm = $("#tool-form");
const fieldsEl = $("#fields");
const emptyHint = $("#empty-hint");
const outputEl = $("#output");
const jobStatus = $("#job-status");
const btnRun = $("#btn-run");
const btnClear = $("#btn-clear-out");

/** @type {{ categories: Array<{ id: string, label: string, tools: Array<{ id: string, label: string, description: string, fields: Field[] }> }> }} */
let catalog = { categories: [] };
/** @type {{ id: string, label: string, description: string, fields: Field[] } | null} */
let currentTool = null;
let eventSource = null;
let running = false;

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
  jobStatus.className = `badge ${kind}`;
  jobStatus.textContent = text;
}

function appendOutput(text) {
  outputEl.textContent += text;
  outputEl.scrollTop = outputEl.scrollHeight;
}

function clearOutput() {
  outputEl.textContent = "";
  setStatus("idle", "就绪");
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

function selectTool(toolId) {
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
  renderFields(found.fields || []);
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

function renderFields(fields) {
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
      cb.checked = defaultValue(field);
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
      if (field.type === "select") {
        input = document.createElement("select");
        for (const opt of field.options || []) {
          const o = document.createElement("option");
          o.value = opt;
          o.textContent = opt || "（无）";
          input.appendChild(o);
        }
        input.value = defaultValue(field);
      } else if (field.type === "number") {
        input = document.createElement("input");
        input.type = "number";
        input.min = field.min ?? 0;
        input.max = field.max ?? 99;
        input.value = defaultValue(field);
      } else {
        input = document.createElement("input");
        input.type = "text";
        input.value = defaultValue(field);
        if (field.required) input.required = true;
      }
      input.name = field.key;
      input.id = `f-${field.key}`;
      row.appendChild(input);

      if (field.type === "file" || field.type === "directory") {
        const browse = document.createElement("button");
        browse.type = "button";
        browse.className = "btn ghost small";
        browse.textContent = hasNativePicker() ? "浏览…" : "路径";
        browse.title = hasNativePicker()
          ? "系统文件对话框"
          : "请手动粘贴本机绝对路径";
        browse.addEventListener("click", async () => {
          const p =
            field.type === "file" ? await pickFile() : await pickDirectory();
          if (p) input.value = p;
          else if (!hasNativePicker()) input.focus();
        });
        row.appendChild(browse);
      }

      wrap.appendChild(row);
      if (field.hint) {
        const h = document.createElement("p");
        h.className = "hint";
        h.textContent = field.hint;
        wrap.appendChild(h);
      }
    }
    fieldsEl.appendChild(wrap);
  }
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
        appendOutput(data.text);
      } else if (data.type === "done") {
        closeEventSource();
        running = false;
        btnRun.disabled = false;
        if (data.error) {
          setStatus("err", "失败");
        } else if (data.exit_code === 0) {
          setStatus("ok", "完成");
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
          for (const line of j.lines || []) appendOutput(line);
          running = false;
          btnRun.disabled = false;
          if (j.exit_code === 0) setStatus("ok", "完成");
          else setStatus("err", `退出码 ${j.exit_code}`);
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
  clearOutput();
  setStatus("running", "运行中…");

  const params = collectParams();
  let res;
  try {
    res = await fetch("/api/run", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ tool_id: currentTool.id, params }),
    });
  } catch (err) {
    appendOutput(`[请求失败] ${err}\n`);
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
    appendOutput(`[错误] ${msg}\n`);
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
  btnClear.addEventListener("click", clearOutput);
}

init().catch((e) => {
  appendOutput(`[启动失败] ${e}\n`);
  setStatus("err", "失败");
});
