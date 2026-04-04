"""Split decorative_screen block model at y=16 into lower/upper files (one-time helper)."""
import json
from pathlib import Path


def clip_el(el: dict, y0: float, y1: float, dy: float):
    """Clip element y-range to [y0, y1), shift resulting y by dy."""
    f = el["from"][1]
    t = el["to"][1]
    lo, hi = min(f, t), max(f, t)
    if hi <= y0 or lo >= y1:
        return None
    nlo, nhi = max(lo, y0), min(hi, y1)
    out = json.loads(json.dumps(el))
    if f < t:
        out["from"][1] = nlo + dy
        out["to"][1] = nhi + dy
    else:
        out["from"][1] = nhi + dy
        out["to"][1] = nlo + dy
    if "rotation" in out:
        o = out["rotation"].get("origin")
        if o:
            oy = o[1]
            if y0 <= oy < y1:
                o[1] = oy + dy
            else:
                o[1] = o[1] + dy
    return out


def main():
    root = Path(__file__).resolve().parents[1]
    p = root / "src/main/resources/assets/fantasy_furniture/models/block/decorative_screen_full.json"
    data = json.loads(p.read_text(encoding="utf-8"))
    base = {k: v for k, v in data.items() if k not in ("elements", "groups")}
    elements = data["elements"]

    lower_els = []
    upper_els = []
    for el in elements:
        lo = clip_el(el, 0, 16, 0)
        if lo:
            lower_els.append(lo)
        up = clip_el(el, 16, 32, -16)
        if up:
            upper_els.append(up)

    lower = dict(base)
    lower["elements"] = lower_els
    upper = dict(base)
    upper["elements"] = upper_els

    out_lo = root / "src/main/resources/assets/fantasy_furniture/models/block/decorative_screen_lower.json"
    out_hi = root / "src/main/resources/assets/fantasy_furniture/models/block/decorative_screen_upper.json"
    out_lo.write_text(json.dumps(lower, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
    out_hi.write_text(json.dumps(upper, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
    print("lower elements", len(lower_els), "upper elements", len(upper_els))


if __name__ == "__main__":
    main()
