package me.yic.xconomy.leaderboard;


import java.math.BigDecimal;
import java.text.NumberFormat;

public class MathUtils {
    public static Boolean Chance(int chance){
        if(Math.random() * 100 < chance) {
            return true;
        }else {
            return false;
        }
    }
    public static String Format(long i){
        return String.format("%,d",i);
    }
    public static String toRome(int number) {
        StringBuilder rNumber = new StringBuilder();
        int[] aArray = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
        String[] rArray = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X",
                "IX", "V", "IV", "I" };
        if (number < 1 || number > 3999) {
            rNumber = new StringBuilder("-1");
        } else {
            for (int i = 0; i < aArray.length; i++) {
                while (number >= aArray[i]) {
                    rNumber.append(rArray[i]);
                    number -= aArray[i];
                }
            }
        }
        return rNumber.toString();
    }
    public static String getPercent(Integer num,Integer totalPeople ) {
        String percent;
        Double p3;
        if(totalPeople == 0){
            p3 = 0.0;
        }else{
            p3 = num*1.0/totalPeople;
        }
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);//控制保留小数点后几位，2：表示保留2位小数点
        percent = nf.format(p3);
        return percent;
    }
    public static String CalculateUtil(BigDecimal a, BigDecimal b){
        String percent =
                b == null ? "-" :
                        b.compareTo(new BigDecimal(0)) == 0 ? "-":
                                a == null ? "0.00%" :
                                        a.multiply(new BigDecimal(100)).divide(b,2,BigDecimal.ROUND_HALF_UP) + "%";
        return percent;
    }
    private static char[] c = new char[]{'k', 'm', 'b', 't'};

    private static String coolFormat(double n, int iteration) {
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) % 10 == 0;
        return (d < 1000 ? ((d > 99.9 || isRound || (!isRound && d > 9.99) ? (int) d * 10 / 10 : d + "") + "" + c[iteration]) : coolFormat(d, iteration + 1));
    }
    public static String FormatChar(double n){
        return coolFormat(n,0);
    }
}
