package com.block.chain.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.ThreadLocalRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.Base64Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 说明：常用工具
 * 创建人：michael
 * 修改时间：
 */
public class ToolUtils {

    private static String randString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * url  编码
     * @param url
     * @return
     */
    public static String getURLEncoder(String url){
        String urls = null;
        try {
            urls = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urls;
    }

    /**
     * url  解码
     * @param url
     * @return
     */
    public static String getURLEncoder1(String url){
        String urls = null;
        try {
            urls = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urls;
    }

    /**
     * 获取随机的字符
     */
    public static String genRandomNum(int num){
        int  maxNum = 36;
        int i;
        int count = 0;
        char[] str = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
                'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
        StringBuffer pwd = new StringBuffer("");
        Random r = new Random();
        while(count < num){
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count ++;
            }
        }
        return pwd.toString();
    }

    /**
     * 随机生成六位数验证码
     *
     * @return
     */
    public static int getRandomNum() {
        Random r = new Random();
        return r.nextInt(900000) + 100000;//(Math.random()*(999999-100000)+100000)
    }

    /**
     * @Description: 产生订单号
     * @param: @param id
     * @param: @param type
     * @param: @return
     * @return: String
     * @author: wz
     * @date: 2021年2月25日 下午12:06:30
     */
    public static String generateOrderID(String id) {
        return id + generateRandom();
    }

    public static String generateRandom() {
        StringBuffer sb = new StringBuffer();
        sb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis()));
        for (int i = 0; i < 4; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 检测字符串是否不为空(null,"","null")
     *
     * @param s
     * @return 不为空则返回true，否则返回false
     */
    public static boolean notEmpty(String s) {
        return s != null && !"".equals(s) && !"null".equals(s);
    }

    /**
     * 检测字符串是否为空(null,"","null")
     *
     * @param s
     * @return 为空则返回true，不否则返回false
     */
    public static boolean isEmpty(String s) {
        return s == null || "".equals(s) || "null".equals(s);
    }

    /**
     * 字符串转换为字符串数组
     *
     * @param str        字符串
     * @param splitRegex 分隔符
     * @return
     */
    public static String[] str2StrArray(String str, String splitRegex) {
        if (isEmpty(str)) {
            return null;
        }
        return str.split(splitRegex);
    }

    /**
     * 用默认的分隔符(,)将字符串转换为字符串数组
     *
     * @param str 字符串
     * @return
     */
    public static String[] str2StrArray(String str) {
        return str2StrArray(str, ",\\s*");
    }

    /**
     * 按照yyyy-MM-dd HH:mm:ss的格式，日期转字符串
     *
     * @param date
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String date2Str(Date date) {
        return date2Str(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 按照yyyy-MM-dd HH:mm:ss的格式，字符串转日期
     *
     * @param date
     * @return
     */
    public static Date str2Date(String date) {
        if (notEmpty(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new Date();
        } else {
            return null;
        }
    }

    /**
     * 按照参数format的格式，日期转字符串
     *
     * @param date
     * @param format
     * @return
     */
    public static String date2Str(Date date, String format) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        } else {
            return "";
        }
    }

    /**
     * 把时间根据时、分、秒转换为时间段
     *
     * @param StrDate
     */
    public static String getTimes(String StrDate) {
        String resultTimes = "";

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now;

        try {
            now = new Date();
            Date date = df.parse(StrDate);
            long times = now.getTime() - date.getTime();
            long day = times / (24 * 60 * 60 * 1000);
            long hour = (times / (60 * 60 * 1000) - day * 24);
            long min = ((times / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long sec = (times / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

            StringBuffer sb = new StringBuffer();
            //sb.append("发表于：");
            if (hour > 0) {
                sb.append(hour + "小时前");
            } else if (min > 0) {
                sb.append(min + "分钟前");
            } else {
                sb.append(sec + "秒前");
            }

            resultTimes = sb.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return resultTimes;
    }

    /**
     * 写txt里的单行内容
     *
     * @param fileP   文件路径
     * @param content 写入的内容
     */
    public static void writeFile(String fileP, String content) {
        String filePath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("")) + "../../";    //项目路径
        filePath = (filePath.trim() + fileP.trim()).substring(6).trim();
        if (filePath.indexOf(":") != 1) {
            filePath = File.separator + filePath;
        }
        try {
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(filePath), "utf-8");
            BufferedWriter writer = new BufferedWriter(write);
            writer.write(content);
            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证邮箱
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码
     *
     * @param mobileNumber
     * @return
     */
    public static boolean checkMobileNumber(String mobileNumber) {
        boolean flag = false;
        try {
            Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 读取txt里的单行内容
     *
     * @param fileP 文件路径
     */
    @SuppressWarnings("resource")
    public static String readTxtFile(String fileP) {
        try {

            String filePath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("")) + "../../";    //项目路径
            filePath = filePath.replaceAll("file:/", "");
            filePath = filePath.replaceAll("%20", " ");
            filePath = filePath.trim() + fileP.trim();
            if (filePath.indexOf(":") != 1) {
                filePath = File.separator + filePath;
            }
            String encoding = "utf-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {        // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);    // 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    return lineTxt;
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件,查看此路径是否正确:" + filePath);
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
        }
        return "";
    }

    /**
     * @param money
     * @Title: InterceptTwoPlaces
     * @Description: 通过四舍五入截取小数点后两位，
     * @return: Double
     */
    public static Double InterceptTwoPlaces(Double money) {
        DecimalFormat df = new DecimalFormat("#.00");
        Double m = Double.parseDouble(df.format(money));
        return m;
    }

    /**
     * @param Bit
     * @param d
     * @return
     * @Title: subDoubleNum
     * @date: 2019年1月17日 上午9:41:31
     * @Description: 截取浮点数后两位
     * @return: String
     */
    public static String subDoubleNum(int Bit, Double d) {
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后多少位
        numberFormat.setMaximumFractionDigits(Bit);
        String num = numberFormat.format(d);
        return num;
    }

    /**
     * 取两个数字间随机数
     */
    public static Integer getRanNum(int min, int max) {
        int num = (int) (Math.random()*(max-min) + min);
        return num;
    }

    /**
     * @param username
     * @return
     * @Title: hideAccount
     * @author: Lzc
     * @date: 2019年8月5日 下午6:26:09
     * @Description: 隐藏账户中间字符串
     * @return: String
     */
    public static String hideAccount(String username) {
        int length = username.length();
        int bit = length / 3;
        StringBuffer hideAccount = new StringBuffer(username.substring(0, bit));
        for (int i = 0; i < length - (bit + 1) - bit; i++) {
            hideAccount.append("*");
        }
        hideAccount.append(username.substring(length - (bit + 1), length));
        return hideAccount.toString();
    }

    /**
     * @Description: json字符串转为对象
     * @param: @param <T>
     * @param: @param json
     * @param: @param type
     * @param: @return
     * @param: @throws IOException
     * @return: T
     * @author: wz
     * @date: 2021年2月24日 上午7:34:13
     */
    public static <T> T convertJson2Object(String json, Class<T> type) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        return mapper.readValue(json, type);
    }

    public static JSONObject getUserInfo(String encryptedData, String sessionKey, String iv) {
        // 被加密的数据
        byte[] dataByte = Base64Utils.decode(encryptedData.getBytes());
        // 加密秘钥
        byte[] keyByte = Base64Utils.decode(sessionKey.getBytes());
        // 偏移量
        byte[] ivByte = Base64Utils.decode(iv.getBytes());
        try {

            // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }

            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher;
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            try {
                AlgorithmParameters parameters;
                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
                parameters = AlgorithmParameters.getInstance("AES");
                parameters.init(new IvParameterSpec(ivByte));
                cipher.init(Cipher.DECRYPT_MODE, spec, parameters);// 初始化
                byte[] resultByte = cipher.doFinal(dataByte);
                if (null != resultByte && resultByte.length > 0) {
                    String result = new String(resultByte, "UTF-8");
                    return JSON.parseObject(result);
                }
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidParameterSpecException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } catch (NoSuchProviderException e) {
            e.getMessage();
        }
        return null;
    }

    /**
     * 根据路径获取流文件
     * @param url
     * @return
     */
    public static String getLyricInfo(String url) {
        String lineTxt = "";
        String AlartTxt="";
        try {
            InputStream fileInputStream = new URL(url).openStream();
            BufferedReader reader = null;
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "Utf-8");
            reader = new BufferedReader(inputStreamReader);

            while((lineTxt = reader.readLine()) != null){
                lineTxt+='\n';
                AlartTxt+=lineTxt;
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AlartTxt;
    }
    // 计算年龄
    public static  int getAge(Date birthDay) throws Exception {
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) { //出生日期晚于当前时间，无法计算
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);  //当前年份
        int monthNow = cal.get(Calendar.MONTH);  //当前月份
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        int age = yearNow - yearBirth;   //计算整岁数
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--;//当前日期在生日之前，年龄减一
            }else{
                age--;//当前月份在生日之前，年龄减一
            }
        } return age;
    }

    /**
     * 将集合按指定数量分组
     * @param list 数据集合
     * @param quantity 分组数量
     * @return 分组结果
     */
    public static <T> List<List<T>> groupListByQuantity(List<T> list, int quantity) {
        if (list == null || list.size() == 0) {
            return null;
        }

        if (quantity <= 0) {
            new IllegalArgumentException("Wrong quantity.");
        }

        List<List<T>> wrapList = new ArrayList<List<T>>();
        int count = 0;
        while (count < list.size()) {
            wrapList.add(new ArrayList<T>(list.subList(count, (count + quantity) > list.size() ? list.size() : count + quantity)));
            count += quantity;
        }

        return wrapList;
    }

    public static <T> List<T> objectCastList(Object obj, Class<T> clazz){
        List<T> result = new ArrayList<>();
        if(obj instanceof List<?>){
            for (Object o : (List<?>) obj){
                result.add(clazz.cast(o));
            }
            return result;
        }
        return new ArrayList<>();
    }

    public static void main(String[] args) {
       /* String url = getURLEncoder("doerweb://www.baidui.com?a=王子");
        System.out.println(url);
        String url1 = getURLEncoder1(url);
        System.out.println(url1);*/
    }

    //获取当天的开始时间 - 毫秒 - 北京时间
    public static   Long getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTimeInMillis();
    }
    //获取当天的结束时间- 毫秒 - 北京时间
    public static Long getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTimeInMillis();
    }
}
