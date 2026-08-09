package org.lanye.fantasy_furniture.content.soap;

/**
 * 肥皂架瓶罐合法组合方案（设计 SOAP-006 / gameplay 方案 1–3）。
 *
 * <p>方案 2 无专用一体 bbmodel；方案 1 / 3 有组合模型档。
 */
public enum SoapRackComboScheme {
    /** 沐浴 + 洗发 + 乳霜 + 有皂架 → {@code combo_full} */
    LIQUIDS_CREAM_RACK_SOAP,
    /** 沐浴/洗发合计 ×3 + 乳霜（不要求有皂） */
    LIQUIDS_X3_CREAM,
    /** 乳霜 ×2 + 有皂架 → {@code combo_cream2}；不可再叠肥皂盒 */
    CREAM2_RACK_SOAP
}
