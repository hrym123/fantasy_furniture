#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""幻想家具 ``tools`` 目录脚本的简易图形启动器（标准库 tkinter，无额外依赖）。

在 ``fantasy_furniture`` 仓库根目录执行::

    python tools/tools_gui.py

子进程的工作目录固定为仓库根，与命令行直接运行 ``python tools/<子目录>/脚本.py`` 一致。

左侧分类与 ``README.md`` 中 ①～⑦ 一致（库脚本 ``bbmodel_to_geojson.py`` 无 CLI，未列入树）。
"""
from __future__ import annotations

import shlex
import subprocess
import sys
import threading
import tkinter as tk
from pathlib import Path
from tkinter import filedialog, messagebox, ttk

FF_ROOT = Path(__file__).resolve().parents[1]
TOOLS_DIR = FF_ROOT / "tools"
if str(TOOLS_DIR) not in sys.path:
    sys.path.insert(0, str(TOOLS_DIR))
from launcher.registry import catalog_for_api  # noqa: E402
from paths import DEFAULT_ASSETS  # noqa: E402
T_BLOCKBENCH = TOOLS_DIR / "blockbench"
T_COLLISION = TOOLS_DIR / "collision"
T_BED6 = TOOLS_DIR / "bed6"
T_GLASS = TOOLS_DIR / "glass"
T_BLOCK_MODEL = TOOLS_DIR / "block_model"


def _browse_file(var: tk.StringVar, *, patterns: tuple[tuple[str, str], ...]) -> None:
    p = filedialog.askopenfilename(filetypes=list(patterns))
    if p:
        var.set(p)


def _browse_dir(var: tk.StringVar) -> None:
    d = filedialog.askdirectory()
    if d:
        var.set(d)


class ToolsGuiApp:
    def __init__(self, root: tk.Tk) -> None:
        self.root = root
        root.title("幻想家具 · 开发工具")
        root.minsize(720, 520)
        root.geometry("900x600")

        self._tool_id: str | None = None
        self._running = False

        main = ttk.PanedWindow(root, orient=tk.HORIZONTAL)
        main.pack(fill=tk.BOTH, expand=True, padx=6, pady=6)

        left = ttk.Frame(main, width=240)
        main.add(left, weight=0)

        right = ttk.Frame(main)
        main.add(right, weight=1)

        ttk.Label(left, text="工具", font=("", 10, "bold")).pack(anchor=tk.W, padx=4, pady=(0, 4))
        tree_frame = ttk.Frame(left)
        tree_frame.pack(fill=tk.BOTH, expand=True)
        self.tree = ttk.Treeview(tree_frame, show="tree", selectmode="browse", height=22)
        ysb = ttk.Scrollbar(tree_frame, orient=tk.VERTICAL, command=self.tree.yview)
        self.tree.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        ysb.pack(side=tk.RIGHT, fill=tk.Y)
        self.tree.configure(yscrollcommand=ysb.set)

        self._fill_tree()
        self.tree.bind("<<TreeviewSelect>>", self._on_tree_select)

        self.detail = ttk.LabelFrame(right, text="说明与参数")
        self.detail.pack(fill=tk.BOTH, expand=True)
        self.form_inner = ttk.Frame(self.detail)
        self.form_inner.pack(fill=tk.BOTH, expand=True, padx=8, pady=8)

        btn_row = ttk.Frame(right)
        btn_row.pack(fill=tk.X, pady=(6, 0))
        self.run_btn = ttk.Button(btn_row, text="运行", command=self._on_run)
        self.run_btn.pack(side=tk.LEFT)
        ttk.Button(btn_row, text="清空输出", command=self._clear_output).pack(side=tk.LEFT, padx=(8, 0))

        out_lab = ttk.Label(right, text="输出")
        out_lab.pack(anchor=tk.W, pady=(8, 2))
        out_wrap = ttk.Frame(right)
        out_wrap.pack(fill=tk.BOTH, expand=True)
        oscroll = ttk.Scrollbar(out_wrap, orient=tk.VERTICAL)
        self.output = tk.Text(out_wrap, height=14, wrap=tk.WORD, font=("Consolas", 9), undo=False)
        oscroll.configure(command=self.output.yview)
        self.output.configure(yscrollcommand=oscroll.set)
        oscroll.pack(side=tk.RIGHT, fill=tk.Y)
        self.output.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

        self._vars: dict[str, tk.Variable] = {}
        self._clear_form()
        ttk.Label(
            self.form_inner,
            text="请从左侧选择一项工具。",
            wraplength=560,
        ).pack(anchor=tk.W)

    def _fill_tree(self) -> None:
        # 与 launcher.registry.TOOL_CATALOG 一致（Web 界面同源）
        for cat in catalog_for_api():
            self.tree.insert("", tk.END, iid=cat["id"], text=cat["label"], open=True)
            for tool in cat["tools"]:
                self.tree.insert(
                    cat["id"],
                    tk.END,
                    iid=tool["id"],
                    text=tool["label"],
                    tags=("tool",),
                )

        self.tree.tag_configure("tool", font=("", 9))

    def _on_tree_select(self, _evt: object | None = None) -> None:
        sel = self.tree.selection()
        if not sel:
            return
        iid = sel[0]
        if self.tree.get_children(iid):
            return
        self._tool_id = iid
        self._build_form(iid)

    def _clear_form(self) -> None:
        for w in self.form_inner.winfo_children():
            w.destroy()
        self._vars.clear()

    def _add_path_row(
        self,
        label: str,
        key: str,
        *,
        patterns: tuple[tuple[str, str], ...],
    ) -> None:
        row = ttk.Frame(self.form_inner)
        row.pack(fill=tk.X, pady=2)
        ttk.Label(row, text=label, width=14).pack(side=tk.LEFT)
        var = tk.StringVar()
        self._vars[key] = var
        ttk.Button(
            row,
            text="浏览…",
            width=8,
            command=lambda: _browse_file(var, patterns=patterns),
        ).pack(side=tk.LEFT, padx=(0, 4))
        ent = ttk.Entry(row, textvariable=var)
        ent.pack(side=tk.LEFT, fill=tk.X, expand=True)

    def _add_dir_row(self, label: str, key: str, default: str = "") -> None:
        row = ttk.Frame(self.form_inner)
        row.pack(fill=tk.X, pady=2)
        ttk.Label(row, text=label, width=14).pack(side=tk.LEFT)
        var = tk.StringVar(value=default)
        self._vars[key] = var
        ttk.Button(row, text="文件夹…", width=8, command=lambda: _browse_dir(var)).pack(
            side=tk.LEFT, padx=(0, 4)
        )
        ent = ttk.Entry(row, textvariable=var)
        ent.pack(side=tk.LEFT, fill=tk.X, expand=True)

    def _add_entry_row(self, label: str, key: str, default: str = "") -> None:
        row = ttk.Frame(self.form_inner)
        row.pack(fill=tk.X, pady=2)
        ttk.Label(row, text=label, width=14).pack(side=tk.LEFT)
        var = tk.StringVar(value=default)
        self._vars[key] = var
        ttk.Entry(row, textvariable=var).pack(side=tk.LEFT, fill=tk.X, expand=True)

    def _add_check(self, label: str, key: str, *, default: bool = False) -> None:
        var = tk.BooleanVar(value=default)
        self._vars[key] = var
        ttk.Checkbutton(self.form_inner, text=label, variable=var).pack(anchor=tk.W, pady=1)

    def _add_spin(self, label: str, key: str, default: int, min_v: int, max_v: int) -> None:
        row = ttk.Frame(self.form_inner)
        row.pack(fill=tk.X, pady=2)
        ttk.Label(row, text=label, width=14).pack(side=tk.LEFT)
        var = tk.IntVar(value=default)
        self._vars[key] = var
        sp = ttk.Spinbox(row, from_=min_v, to=max_v, textvariable=var, width=8)
        sp.pack(side=tk.LEFT)

    def _add_combo(self, label: str, key: str, values: tuple[str, ...], default: str = "") -> None:
        row = ttk.Frame(self.form_inner)
        row.pack(fill=tk.X, pady=2)
        ttk.Label(row, text=label, width=14).pack(side=tk.LEFT)
        var = tk.StringVar(value=default)
        self._vars[key] = var
        ttk.Combobox(row, textvariable=var, values=values, width=36, state="readonly").pack(
            side=tk.LEFT, fill=tk.X, expand=True
        )

    def _add_desc(self, text: str) -> None:
        ttk.Label(self.form_inner, text=text, wraplength=620, justify=tk.LEFT).pack(
            anchor=tk.W, pady=(0, 8)
        )

    def _add_extra_args(self) -> None:
        ttk.Label(self.form_inner, text="附加参数（按 shell 规则，可选）", font=("", 9, "bold")).pack(
            anchor=tk.W, pady=(10, 2)
        )
        var = tk.StringVar()
        self._vars["extra_args"] = var
        ttk.Entry(self.form_inner, textvariable=var).pack(fill=tk.X)

    def _build_form(self, tid: str) -> None:
        self._clear_form()
        if tid == "export_bbmodel":
            self._add_desc(
                "将 .bbmodel 导出到 assets（geo、贴图、动画）。需要 Pillow/NumPy 时使用玻璃窗共享贴图。"
            )
            self._add_path_row(
                ".bbmodel",
                "path",
                patterns=[("Blockbench", "*.bbmodel"), ("所有文件", "*.*")],
            )
            self._add_entry_row("asset-id", "asset_id", "")
            self._add_dir_row(
                "导出目标（assets 根）",
                "assets_root",
                default=str(DEFAULT_ASSETS),
            )
            self._add_check("仅预览（--dry-run）", "dry_run")
            self._add_combo(
                "共享贴图键",
                "shared",
                ("", "plain_glass_window"),
                "",
            )
            self._add_check("仅主贴图槽（--only-primary-texture）", "only_primary")
            self._add_check("跳过 geo（--skip-geo）", "skip_geo")
            self._add_check("跳过贴图（--skip-textures）", "skip_textures")
            self._add_check("无动画时删旧 animation（--delete-stale-animation）", "del_anim")
            self._add_extra_args()
        elif tid == "split_screen":
            self._add_desc(
                "读取 models/block/decorative_screen_full.json，写出 lower / upper。"
                " 无参数；请确认文件存在。"
            )
        elif tid == "geo_collision":
            self._add_desc("从 geo.json 或方块模型 JSON 计算外接碰撞盒。详见 tools/collision/geo_collision_box.py。")
            self._add_path_row(
                "输入文件",
                "path",
                patterns=[("JSON", "*.json"), ("所有文件", "*.*")],
            )
            self._add_check("原始并集（--raw）", "raw")
            self._add_check("输出 Java（--emit-java）", "emit_java")
            self._add_check("实体碰撞盒（--entity-hitbox）", "entity_hit")
            self._add_check("方块模型 JSON（--mc-block-model）", "mc_block")
            self._add_spin("小数位", "precision", 2, 0, 8)
            self._add_extra_args()
        elif tid == "block_collision":
            self._add_desc("逐 cube 多盒碰撞明细。详见 tools/collision/block_collision_detail.py。")
            self._add_path_row("geo.json", "path", patterns=[("JSON", "*.json"), ("所有文件", "*.*")])
            self._add_combo("格式", "fmt", ("text", "json", "markdown"), "text")
            self._add_spin("小数位", "precision", 4, 0, 8)
            self._add_check("跳过空交（--skip-empty）", "skip_empty")
            self._add_check("Java Shapes.or（--java-or）", "java_or")
            self._add_check("Java orParts（--java-or-parts）", "java_parts")
            self._add_extra_args()
        elif tid in ("voxel_pick", "bed_voxel"):
            if tid == "bed_voxel":
                self._add_desc("床板6：等同通用选取 + preset bed-plate6（z∈[0,32] 等）。")
            else:
                self._add_desc("从 geo 生成北向选取 VoxelShape Java（默认单格 [0,16]³）。")
            self._add_path_row("geo.json", "path", patterns=[("JSON", "*.json"), ("所有文件", "*.*")])
            if tid == "voxel_pick":
                self._add_combo("系列预设", "preset", ("", "bed-plate6"), "")
            self._add_check("半格量化（--snap-half）", "snap_half")
            self._add_entry_row("min-extent", "min_extent", "0.5")
            self._add_spin("Java 小数位", "precision", 4, 0, 8)
            self._add_entry_row("方法名", "method", "buildPickShapeNorthUnionGenerated")
            self._add_extra_args()
        elif tid == "export_duvet":
            self._add_desc("export_bed_plate6_duvet_textures_from_bbmodel：默认路径见脚本，可覆盖。")
            self._add_path_row("bbmodel（可选）", "bbmodel", patterns=[("bbmodel", "*.bbmodel"), ("所有文件", "*.*")])
            self._add_dir_row("输出目录（可选）", "out_dir")
            self._add_extra_args()
        elif tid == "export_duvet_cover":
            self._add_desc("export_bed_plate6_duvet_cover_textures_from_bbmodel。")
            self._add_path_row("bbmodel（可选）", "bbmodel", patterns=[("bbmodel", "*.bbmodel"), ("所有文件", "*.*")])
            self._add_dir_row("输出目录（可选）", "out_dir")
            self._add_extra_args()
        elif tid == "export_pillow_medium":
            self._add_desc("export_bed_plate6_pillow_medium_textures_from_bbmodel。")
            self._add_path_row("bbmodel（可选）", "bbmodel", patterns=[("bbmodel", "*.bbmodel"), ("所有文件", "*.*")])
            self._add_extra_args()
        elif tid == "export_bed_png":
            self._add_desc(
                "脚本内硬编码 MoonStarfish 床板6.bbmodel 路径；本机若无该文件会失败。"
            )
        elif tid == "extract_pillow_large":
            self._add_desc("脚本内硬编码 MoonStarfish 目录；仅当路径存在时可用。")
        elif tid == "duvet_rename":
            self._add_desc("按主色重写被单 bbmodel 内 texture 显示名；默认路径见脚本。")
            self._add_path_row("bbmodel（可选）", "bbmodel", patterns=[("bbmodel", "*.bbmodel"), ("所有文件", "*.*")])
            self._add_check("写回文件（--write）", "write")
            self._add_extra_args()
        elif tid == "glass_lang":
            self._add_desc("对照玻璃窗贴图主色与译名。勾选写回语言文件。")
            self._add_check("写回 zh_cn / en_us（--write）", "write")
        elif tid == "pillow_lang":
            self._add_desc("打印枕头系列 PNG 主色，不写文件。")
        elif tid == "test_voxel":
            self._add_desc("运行 tools/collision 与 tools/bed6 的 voxel_pick 单测。")
        else:
            ttk.Label(self.form_inner, text="未知工具").pack(anchor=tk.W)

    def _append_output(self, s: str) -> None:
        self.output.insert(tk.END, s)
        self.output.see(tk.END)

    def _clear_output(self) -> None:
        self.output.delete("1.0", tk.END)

    def _extra_argv(self) -> list[str]:
        ex = self._vars.get("extra_args")
        if not ex:
            return []
        raw = str(ex.get()).strip()
        if not raw:
            return []
        try:
            return shlex.split(raw, posix=(sys.platform != "win32"))
        except ValueError as e:
            messagebox.showerror("附加参数", f"无法解析: {e}")
            return []

    def _build_cmd(self) -> list[str] | None:
        tid = self._tool_id
        if not tid:
            messagebox.showinfo("提示", "请先在左侧选择工具。")
            return None
        extra = self._extra_argv()

        def path_req(key: str = "path") -> Path | None:
            s = str(self._vars[key].get()).strip()
            if not s:
                messagebox.showerror("参数", "请选择或填写输入文件路径。")
                return None
            p = Path(s)
            if not p.is_file():
                messagebox.showerror("参数", f"文件不存在:\n{p}")
                return None
            return p

        if tid == "export_bbmodel":
            p = path_req("path")
            if not p:
                return None
            cmd = [str(T_BLOCKBENCH / "export_bbmodel_to_fantasy_furniture_assets.py"), str(p)]
            aid = str(self._vars["asset_id"].get()).strip()
            if aid:
                cmd += ["--asset-id", aid]
            if self._vars["dry_run"].get():
                cmd.append("--dry-run")
            sh = str(self._vars["shared"].get()).strip()
            if sh:
                cmd += ["--shared-textures", sh]
            if self._vars["only_primary"].get():
                cmd.append("--only-primary-texture")
            if self._vars["skip_geo"].get():
                cmd.append("--skip-geo")
            if self._vars["skip_textures"].get():
                cmd.append("--skip-textures")
            if self._vars["del_anim"].get():
                cmd.append("--delete-stale-animation")
            ar = str(self._vars["assets_root"].get()).strip()
            if ar:
                cmd += ["--assets-root", ar]
            return cmd + extra

        if tid == "split_screen":
            return [str(T_BLOCK_MODEL / "split_screen_model.py")] + extra

        if tid == "geo_collision":
            p = path_req()
            if not p:
                return None
            cmd = [str(T_COLLISION / "geo_collision_box.py"), str(p)]
            if self._vars["raw"].get():
                cmd.append("--raw")
            if self._vars["emit_java"].get():
                cmd.append("--emit-java")
            if self._vars["entity_hit"].get():
                cmd.append("--entity-hitbox")
            if self._vars["mc_block"].get():
                cmd.append("--mc-block-model")
            cmd += ["--precision", str(int(self._vars["precision"].get()))]
            return cmd + extra

        if tid == "block_collision":
            p = path_req()
            if not p:
                return None
            cmd = [
                str(T_COLLISION / "block_collision_detail.py"),
                str(p),
                "--format",
                str(self._vars["fmt"].get()),
                "--precision",
                str(int(self._vars["precision"].get())),
            ]
            if self._vars["skip_empty"].get():
                cmd.append("--skip-empty")
            if self._vars["java_or"].get():
                cmd.append("--java-or")
            if self._vars["java_parts"].get():
                cmd.append("--java-or-parts")
            return cmd + extra

        if tid in ("voxel_pick", "bed_voxel"):
            p = path_req()
            if not p:
                return None
            cmd = [str(T_COLLISION / "voxel_pick_from_geo.py"), str(p)]
            if tid == "bed_voxel":
                cmd += ["--preset", "bed-plate6"]
            else:
                pr = str(self._vars["preset"].get()).strip()
                if pr:
                    cmd += ["--preset", pr]
            if self._vars["snap_half"].get():
                cmd.append("--snap-half")
            me = str(self._vars["min_extent"].get()).strip()
            if me:
                cmd += ["--min-extent", me]
            cmd += ["--precision", str(int(self._vars["precision"].get()))]
            mn = str(self._vars["method"].get()).strip()
            if mn:
                cmd += ["--method-name", mn]
            return cmd + extra

        if tid == "export_duvet":
            cmd = [str(T_BED6 / "export_bed_plate6_duvet_textures_from_bbmodel.py")]
            b = str(self._vars["bbmodel"].get()).strip()
            o = str(self._vars["out_dir"].get()).strip()
            if b:
                cmd += ["--bbmodel", b]
            if o:
                cmd += ["--out-dir", o]
            return cmd + extra

        if tid == "export_duvet_cover":
            cmd = [str(T_BED6 / "export_bed_plate6_duvet_cover_textures_from_bbmodel.py")]
            b = str(self._vars["bbmodel"].get()).strip()
            o = str(self._vars["out_dir"].get()).strip()
            if b:
                cmd += ["--bbmodel", b]
            if o:
                cmd += ["--out-dir", o]
            return cmd + extra

        if tid == "export_pillow_medium":
            cmd = [str(T_BED6 / "export_bed_plate6_pillow_medium_textures_from_bbmodel.py")]
            b = str(self._vars["bbmodel"].get()).strip()
            if b:
                cmd += ["--bbmodel", b]
            return cmd + extra

        if tid == "export_bed_png":
            return [str(T_BED6 / "export_bed_plate6_texture_from_bbmodel.py")] + extra

        if tid == "extract_pillow_large":
            return [str(T_BED6 / "extract_bed_plate6_pillow_large_textures_from_bbmodel.py")] + extra

        if tid == "duvet_rename":
            cmd = [str(T_BED6 / "bed_plate6_duvet_bbmodel_rename_textures_by_color.py")]
            b = str(self._vars["bbmodel"].get()).strip()
            if b:
                cmd += ["--bbmodel", b]
            if self._vars["write"].get():
                cmd.append("--write")
            return cmd + extra

        if tid == "glass_lang":
            cmd = [str(T_GLASS / "plain_glass_window_lang_display_colors.py")]
            if self._vars["write"].get():
                cmd.append("--write")
            return cmd + extra

        if tid == "pillow_lang":
            return [str(T_BED6 / "bed_plate6_pillow_lang_display_colors.py")] + extra

        if tid == "test_voxel":
            return [str(TOOLS_DIR / "test_voxel_pick_all.py")] + extra

        messagebox.showerror("内部错误", f"未实现: {tid}")
        return None

    def _on_run(self) -> None:
        if self._running:
            return
        cmd_tail = self._build_cmd()
        if cmd_tail is None:
            return
        cmd = [sys.executable, "-u"] + cmd_tail
        self._running = True
        self.run_btn.configure(state=tk.DISABLED)
        self._append_output(f"$ {' '.join(cmd)}\n{'='*60}\n")

        def work() -> None:
            try:
                proc = subprocess.run(
                    cmd,
                    cwd=str(FF_ROOT),
                    capture_output=True,
                    text=True,
                    encoding="utf-8",
                    errors="replace",
                    timeout=3600,
                )
                out = (proc.stdout or "") + (proc.stderr or "")
                code = proc.returncode
            except subprocess.TimeoutExpired:
                out = "[超时]\n"
                code = -1
            except Exception as e:
                out = f"[异常] {e}\n"
                code = -1

            def finish() -> None:
                self._append_output(out)
                if code != 0:
                    self._append_output(f"\n进程退出码: {code}\n")
                self._running = False
                self.run_btn.configure(state=tk.NORMAL)

            self.root.after(0, finish)

        threading.Thread(target=work, daemon=True).start()


def main() -> None:
    if not FF_ROOT.is_dir() or not (FF_ROOT / "src").is_dir():
        print("请在 fantasy_furniture 仓库中运行 tools/tools_gui.py", file=sys.stderr)
        sys.exit(1)
    root = tk.Tk()
    ToolsGuiApp(root)
    root.mainloop()


if __name__ == "__main__":
    main()
