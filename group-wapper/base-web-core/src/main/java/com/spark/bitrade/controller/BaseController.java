package com.spark.bitrade.controller;


import com.spark.bitrade.core.DataTableRunner;
import com.spark.bitrade.core.Pagination;
import com.spark.bitrade.util.DataTable;
import com.spark.bitrade.util.DataTableRequest;
import com.spark.bitrade.util.MessageResult;
import com.sparkframework.lang.Convert;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

public class BaseController {
    private final String MOBILE_THEME_PREFIX = "mobile/";
    private final String DEFAULT_THEME_PREFIX = "";
    private final boolean ENABLE_MOBILE = false;
    private static Log log = LogFactory.getLog(BaseController.class);
    protected String viewPath;
    private PrintWriter out;
    //protected Map<String, String> paramMap = new HashMap<String, String>();



    protected MessageResult success() {
        return new MessageResult(0, "SUCCESS");
    }

    protected MessageResult success(String msg) {
        return new MessageResult(0, msg);
    }

    protected MessageResult success(String msg, Object obj) {
        MessageResult mr = new MessageResult(0, msg);
        mr.setData(obj);
        return mr;
    }

    protected MessageResult success(Object obj) {
        MessageResult mr = new MessageResult(0, "SUCCESS");
        mr.setData(obj);
        return mr;
    }

    protected MessageResult error(String msg) {
        return new MessageResult(500, msg);
    }

    protected MessageResult error(int code, String msg) {
        return new MessageResult(code, msg);
    }

    /**
     * 返回tpl路径,根据浏览器返回不同主题
     *
     * @param name
     * @return
     * @throws Exception
     */
    protected String view(HttpServletRequest request, String name) {
        // 模板路径
        String path = StringUtils.isEmpty(viewPath) ? name : viewPath + "/" + name;
        // 模板名称
        String tpl = null;
        if (ENABLE_MOBILE && isMobile(request) && !viewPath.contains("admin")) {
            tpl = MOBILE_THEME_PREFIX + path;
        } else {
            tpl = DEFAULT_THEME_PREFIX + path;
        }
        System.out.println(tpl);
        return tpl;
    }

    /**
     * 使用MOBILE页面
     *
     * @param name
     * @return
     */
    protected String viewMobile(String name) {
        log.info("viewMobile");
        // 模板路径
        String path = StringUtils.isEmpty(viewPath) ? name : viewPath + "/" + name;
        // 模板名称
        String tpl = MOBILE_THEME_PREFIX + path;
        return tpl;
    }

    public PrintWriter getOut(HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        out = response.getWriter();
        return out;
    }

    protected void sendScript(HttpServletResponse response, String script) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write("<script>" + script + "</script>");
    }

    protected void sendHtml(HttpServletResponse response, String html) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(html);
    }

    /**
     * 获取request请求参数
     *
     * @param name 参数名称
     * @return
     */
    protected String request(HttpServletRequest request, String name) {
        return StringUtils.trimToEmpty(request.getParameter(name));
    }

    protected String requestString(HttpServletRequest request, String name) {
        return Convert.strToStr(request.getParameter(name), "");
    }

    protected Double requestDouble(HttpServletRequest request, String name) {
        return Convert.strToDouble(request.getParameter(name), 0D);
    }

    protected int requestInt(HttpServletRequest request, String name) {
        return Convert.strToInt(request.getParameter(name), 0);
    }

    protected long requestLong(HttpServletRequest request, String name) {
        return Convert.strToLong(request.getParameter(name), 0);
    }

    protected int requestInt(HttpServletRequest request, String name, int def) {
        return Convert.strToInt(request.getParameter(name), def);
    }

    public String getRemoteIp(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
            return request.getHeader("X-Real-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
            return request.getHeader("X-Forwarded-For");
        } else if (StringUtils.isNotBlank(request.getHeader("Proxy-Client-IP"))) {
            return request.getHeader("Proxy-Client-IP");
        }
        return request.getRemoteAddr();
    }

    /**
     * 读取表格数据
     *
     * @param runner
     * @return
     */
    public DataTable dataTable(HttpServletRequest request, DataTableRunner runner) {
        new DataTable();
        DataTableRequest params = new DataTableRequest();
        params.setDraw(Convert.strToInt(request(request, "draw"), 0));
        params.setStart(Convert.strToInt(request(request, "start"), 0));
        params.setLength(Convert.strToInt(request(request, "length"), 0));
        params.parseRequest(request);
        try {
            DataTable dt = runner.run(params);
            dt.setDraw(params.getDraw());
            return dt;
        } catch (Exception e) {
            e.printStackTrace();
            DataTable dt = new DataTable();
            List<Map<String, String>> data = new ArrayList<Map<String, String>>();
            dt.setData(data);
            dt.setDraw(params.getDraw());
            dt.setError("读取数据失败");
            return dt;
        }
    }

    public DataTable dataTable(HttpServletRequest request, DataTableRunner runner, String[] titles) {
        DataTable dt = dataTable(request, runner);
        dt.setTitles(Arrays.asList(titles));
        return dt;
    }

    /**
     * 判断是否是手机浏览器
     *
     * @return
     */
    public boolean isMobile(HttpServletRequest request) {
        boolean isMobile = false;
        //String hostname = request.getHeader("Host");
        // Config cfg = ApplicationUtil.getBean(Config.class);
        /*
         * if(cfg.getWapDomain().equalsIgnoreCase(hostname)){ return true; }
         */
        String[] mobileAgents = {"iphone", "android", "phone", "ipad", "mobile", "wap", "netfront", "java",
                "opera mobi", "opera mini", "ucweb", "windows ce", "symbian", "series", "webos", "sony", "blackberry",
                "dopod", "nokia", "samsung", "palmsource", "xda", "pieplus", "meizu", "midp", "cldc", "motorola",
                "foma", "docomo", "up.browser", "up.link", "blazer", "helio", "hosin", "huawei", "novarra", "coolpad",
                "webos", "techfaith", "palmsource", "alcatel", "amoi", "ktouch", "nexian", "ericsson", "philips",
                "sagem", "wellcom", "bunjalloo", "maui", "smartphone", "iemobile", "spice", "bird", "zte-", "longcos",
                "pantech", "gionee", "portalmmm", "jig browser", "hiptop", "benq", "haier", "^lct", "320x320",
                "240x320", "176x220", "w3c ", "acs-", "alav", "alca", "amoi", "audi", "avan", "benq", "bird", "blac",
                "blaz", "brew", "cell", "cldc", "cmd-", "dang", "doco", "eric", "hipt", "inno", "ipaq", "java", "jigs",
                "kddi", "keji", "leno", "lg-c", "lg-d", "lg-g", "lge-", "maui", "maxo", "midp", "mits", "mmef", "mobi",
                "mot-", "moto", "mwbp", "nec-", "newt", "noki", "oper", "palm", "pana", "pant", "phil", "play", "port",
                "prox", "qwap", "sage", "sams", "sany", "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem",
                "smal", "smar", "sony", "sph-", "symb", "t-mo", "teli", "tim-", "tsm-", "upg1", "upsi", "vk-v", "voda",
                "wap-", "wapa", "wapi", "wapp", "wapr", "webc", "winw", "winw", "xda", "xda-", "Googlebot-Mobile"};
        // 根据cookie选择模板，暂时未用
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().toUpperCase().equals("SPARK_THEME")) {
                    log.info("SPAKR_THEME:" + cookie.getValue());
                    isMobile = cookie.getValue().equals("WAP");
                    break;
                }
            }
        }
        // 根据userAgent自动选择
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        if (request.getHeader("User-Agent") != null) {
            for (String mobileAgent : mobileAgents) {
                if (userAgent.indexOf(mobileAgent) >= 0) {
                    log.info("User-Agent HIT:" + mobileAgent);
                    isMobile = true;
                    break;
                }
            }
        }
        return isMobile;
    }

    public Pagination getPage(HttpServletRequest request) {
        int pageSize = requestInt(request, "pageSize", 10);
        int curPage = requestInt(request, "curPage", 1);
        return new Pagination(curPage, pageSize);
    }

    public Map<String, String> getParameters(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        Map<String, String> map = new HashMap<String, String>();
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            map.put(key, String.join(";", values));
        }
        return map;
    }

    protected String getPhysicalPath(HttpServletRequest request) {
        return request.getSession().getServletContext().getRealPath("/");
    }

    public String createForwardForm(String gateway, String method, Map<String, String> map) {
        StringBuffer sb = new StringBuffer(
                "<html><head <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><script type='text/javascript'>window.onload=function(){document.getElementById('submitForm').submit();}</script></head><body>");
        // 表单内容
        sb.append("<form action='" + gateway + "'  id='submitForm' method='" + method + "'>");
        if (map != null) {
            map.forEach((key, value) -> {
                try {
                    sb.append("<input type='hidden' name='" + key + "'  value='" + value + "'  />");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        sb.append("</form></body></html>");
        return sb.toString();
    }

    public String htmlError(String message) {
        return "<html><body><h2>" + message + "</h2></body></html>";
    }

    //文件下载

}
