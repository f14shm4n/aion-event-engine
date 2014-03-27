package pirate.utils;

import org.slf4j.LoggerFactory;

/**

 @author flashman
 */
public class TimeUtil {

    /**
     Преобразуется время выраженное в секундах, в формат <tt>час,минута,секунда</tt>

     @param time время в секундах
     @return <tt>int[hours,minutes,seconds]</tt>
     */
    public static int[] getHMS(int time) {
        int[] hms = new int[3];

        int s = getClearValue(time);
        int m = getClearValue((int) Math.floor(time / 60));
        int h = (int) Math.floor((time / 60) / 60);

        hms[0] = h;
        hms[1] = m;
        hms[2] = s;

        return hms;
    }

    /**
     Конвертирует массив содержащий h m s в строковое представление.

     @param hms массив h m s
     @return строка вида - <tt>0 ч. 0 м. 0 с.</tt>
     */
    public static String convertToString(int[] hms) {
        if (hms[0] > 0) {
            return String.format("%s ч. %s м. %s с.", hms[0], hms[1], hms[2]);
        }
        if (hms[1] > 0) {
            return String.format("%s м. %s с.", hms[1], hms[2]);
        }
        return String.format("%s с.", hms[2]);
    }

    /**
     Конвертирует время в секундах в строковое представление.

     @param time время в секундах
     @return строка вида - <tt>0 ч. 0 м. 0 с.</tt>
     */
    public static String convertToString(int time) {
        try {
            return convertToString(getHMS(time));
        } catch (Exception ex) {
            LoggerFactory.getLogger(TimeUtil.class).error("Error while converting time: ", ex);
            return null;
        }
    }

    private static int getClearValue(int time) {
        int v1 = (int) Math.floor(time / 60);
        int v2 = time - v1 * 60;
        return v2;
    }
}
