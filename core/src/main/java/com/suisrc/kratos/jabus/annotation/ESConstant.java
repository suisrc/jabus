package com.suisrc.kratos.jabus.annotation;

/**
 * External Subscribe Constant
 */
public class ESConstant {
    private ESConstant() {}
    
    // ?} 替换的内容
    public static final String DESN = ".destination}";
    public static final String SUFK = "?}"; // '?}' = '.destination'

    // prefix key => PK
    public static final String PK_IN0  = "$<";  // '$<xxxx' = '${#xxxx-in-0?}'  主题专用
    public static final String PK_OUT0 = "$>";  // '$>xxxx' = '${#xxxx-out-0?}' 主题专用
    public static final String PK_GG   = "$$";  // '$$' = '${#xxxx-in-0.group}' 需要配合$<一起使用，读取PK_IN0中的内容
}
