package com.github.dn3tao.calcfortraitor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Вычисляет поправку и угол
 *
 * @author dn3tao
 */

class Calculator {

    private static float elev = 0;
    private static float dper100 = 0;
    private static int[][] sel_charge = null; // range из выбранного charge

    private static float axis_angle = 0; // угол между целью и ближайшей осью координат
    private static float azimute = 0; // азимут на цель
    private static int range = 0;
    private static int new_range = 0;

    /**
     * возвращает номер в массиве ближайшего меньшего
     *
     * @param ints  массив в котором ведется поиск
     * @param range значение к которому ищется ближайшее меньшее
     */
    static private int nearest(int[][] ints, int range) {

        int closest = 0;
        for (int i = ints.length - 1; i >= 0; i--) {
            if (range < ints[i][0]) {
                closest = i - 1;
            }
        }
//        Log.d(TAG, "nearest: " + closest + " " + ints[closest][0]);
        return closest;
    }

    /**
     * нахождение ближайшего бОльшего значения Range
     */
    static private int nearest_next(int[][] ints, int range) {
        int closest = 0;
        for (int i = ints.length - 1; i >= 0; i--) {
            if (range < ints[i][0]) {
                closest = i;
            }
        }
//        Log.d(TAG, "nearest-next: " + closest + " " + ints[closest][0]);
        return closest;


    }

    /**
     * Вычисление угла между целью и ближайшей осью координат
     */
    static float axis_angle_calc(int x1, int y1, int x2, int y2) {
        double f_x2 = (double) x2;
        double f_y2 = (double) y2;
        double f_x1 = (double) x1;
        double f_y1 = (double) y1;
        double result = (Math.asin((f_x2 - f_x1) / (Math.sqrt((Math.pow((f_x2 - f_x1), 2)) + (Math.pow((f_y2 - f_y1), 2))))) * 1018.591636);
//        Log.d(TAG, "axis_angle_calc: " + result);
        return (float) result;
    }

    /**
     * Вычисление азимута
     */
    static float azimute_calc(int x1, int y1, int x2, int y2, float axis_angle) {
        if (x2 > x1 && y2 > y1) {
            return (0 + Math.abs(axis_angle));
        } else if (x2 > x1 && y2 < y1) {
            return (3200 - Math.abs(axis_angle));
        } else if (x2 < x1 && y2 < y1) {
            return (3200 + Math.abs(axis_angle));
        } else {
            return (6400 - Math.abs(axis_angle));
        }
    }


    /**
     * вычисляем range
     */
    static int range_calc(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }


    /**
     * Далее проверяем, если разница между текущим значением Range и следующим ближайшим в бОльшую
     * сторону меньше 10 (например range=2446, следующий 2450, 2450-2446=4), то значение Range
     * округляем в бОльшую сторону к следующему ближайшему значению
     */
    static private void check_range(int[][] ints, int range) {
        int closest = nearest_next(ints, range);
        if (ints[closest][0] - range <= 10) {
            new_range = ints[closest][0];
        }
//        Log.d(TAG, "new range: " + new_range + " closest: " + ints[closest][0] + " range : " + range);
    }

    /**  Вычисляем линейное изменения Elevation для каждых 10м,
     *   в промежутке между ближайшим меньшим и ближайшим бОльшим табличными значениями Range
     */
    static private float elev_calc(float dper100, int[][] select) {
        // dper10=((dper100 для меньшего range)-(dper100 для большего range))/5;
        int nearest = nearest(select, new_range);
        int nearest_next = nearest_next(select, new_range);
//        Log.d(TAG, "local nearest : " + select[nearest][1] + " local nearest_next : " + select[nearest_next][1]);
        float dper10 = ((float) select[nearest][1] - (float) select[nearest_next][1]) / (float) 5.0;
        float multiply = ((new_range % 100) - (new_range % 10)) / 10; // TODO исправить проблему с типами
        float exact_D = dper10 * multiply;
//        Log.d(TAG, "dper10: " + dper10);
        return exact_D;
    }

    /**
     * Главый метод класса, возвращает результат вычислений
     *
     * @param caliber   выбранный калибр
     * @param altTarget высота над уровнем моря цели
     * @param altMortar высота над уровнем моря стрелка
     * @param x1        координаты себя
     * @param y1        координаты себя
     * @param y2        координаты себя
     * @param x2        координаты цели
     * @return float с результатами вычислений
     */
    static HashMap<String, Float> calculate(int caliber, int altTarget, int altMortar, int x1, int y1, int x2, int y2) {
        float target = (float) altTarget;
        float mortar = (float) altMortar;
        float fix;
        float result_f;
        float exact_D = 0;

        HashMap<String, Float> resultHash = new HashMap<>();

        range = range_calc(x1, y1, x2, y2);
        resultHash.put("range", (float) range);
//        Log.d(TAG, "range: " + range);

        axis_angle = axis_angle_calc(x1, y1, x2, y2);
//        Log.d(TAG, "axis_angle: " + axis_angle);

        azimute = azimute_calc(x1, y1, x2, y2, axis_angle);
        resultHash.put("azimute", azimute);
//        Log.d(TAG, "azimute: " + azimute);


        ArrayList<String> select = selectCharge(range, caliber);
//        Log.d(TAG, "selected array" + select);
        if (select.size() > 0) {
            for (int i = select.size(); i > 0; i--) {
                setNDE(select.get(i - 1), caliber, range);
                new_range = range;
                check_range(sel_charge, range);
                setNDE(select.get(i - 1), caliber, new_range);

//                Log.d(TAG, "dper100: " + dper100 + " " + select.get(i - 1));
//                Log.d(TAG, "elev: " + elev + " " + select.get(i - 1));

                exact_D = elev_calc(dper100, sel_charge);
//                Log.d(TAG, "new elev: " + exact_D);

                if (altTarget - altMortar >= 0) {
                    fix = (((target - mortar) / 100) * dper100);
                } else {
                    fix = (((mortar - target) / 100) * dper100);
                }

                if (altTarget - altMortar >= 0) {
                    result_f = ((elev - fix) - exact_D);
                } else {
                    result_f = ((elev + fix) + exact_D);
                }
                resultHash.put(select.get(i - 1), result_f);
//                Log.d(TAG, "-------------------");
            }
        }

//        Log.d(TAG, "caliber: " + caliber);
        return resultHash;
    }

    static private ArrayList<String> selectCharge(int range, int caliber) {
        ArrayList<String> result_array = new ArrayList<>();
        if (caliber == 120) {
            if (CrAr.charge0_120[0][0] <= range && range <= CrAr.charge0_120[CrAr.charge0_120.length - 1][0]) {
                result_array.add("Charge 0");
            }
            if (CrAr.charge1_120[0][0] <= range && range <= CrAr.charge1_120[CrAr.charge1_120.length - 1][0]) {
                result_array.add("Charge 1");
            }
            if (CrAr.charge2_120[0][0] <= range && range <= CrAr.charge2_120[CrAr.charge2_120.length - 1][0]) {
                result_array.add("Charge 2");
            }
            if (CrAr.charge3_120[0][0] <= range && range <= CrAr.charge3_120[CrAr.charge3_120.length - 1][0]) {
                result_array.add("Charge 3");
            }
            if (CrAr.charge4_120[0][0] <= range && range <= CrAr.charge4_120[CrAr.charge4_120.length - 1][0]) {
                result_array.add("Charge 4");
            }
            if (CrAr.charge5_120[0][0] <= range && range <= CrAr.charge5_120[CrAr.charge5_120.length - 1][0]) {
                result_array.add("Charge 5");
            }
            if (CrAr.charge6_120[0][0] <= range && range <= CrAr.charge6_120[CrAr.charge6_120.length - 1][0]) {
                result_array.add("Charge 6");
            }
        } else if (caliber == 82) {
            if (CrAr.charge0_82[0][0] <= range && range <= CrAr.charge0_82[CrAr.charge0_82.length - 1][0]) {
                result_array.add("Charge 0");
            }
            if (CrAr.charge1_82[0][0] <= range && range <= CrAr.charge1_82[CrAr.charge1_82.length - 1][0]) {
                result_array.add("Charge 1");
            }
            if (CrAr.charge2_82[0][0] <= range && range <= CrAr.charge2_82[CrAr.charge2_82.length - 1][0]) {
                result_array.add("Charge 2");
            }
        } else {
            if (CrAr.charge0_30[0][0] <= range && range <= CrAr.charge0_30[CrAr.charge0_30.length - 1][0]) {
                result_array.add("Charge 0");
            }
            if (CrAr.charge1_30[0][0] <= range && range <= CrAr.charge1_30[CrAr.charge1_30.length - 1][0]) {
                result_array.add("Charge 1");
            }
            if (CrAr.charge2_30[0][0] <= range && range <= CrAr.charge2_30[CrAr.charge2_30.length - 1][0]) {
                result_array.add("Charge 2");
            }
        }
        return result_array;
    }

    /**
     * проверяет лежит ли range в пределах выбранного charge
     *
     * @param caliber выбранный калибр
     * @return результат проверки
     */
    static boolean check(int x1, int y1, int x2, int y2, int caliber) {
        int range = range_calc(x1, y1, x2, y2);
        ArrayList<String> select_array = selectCharge(range, caliber);
        int start = 0;
        int end = 0;
        String select;
        for (int i = select_array.size(); i > 0; i--) {
            select = select_array.get(i - 1);
            if (caliber == 120) {
                switch (select) {
                    case "Charge 0":
                        start = CrAr.charge0_120[0][0];
                        end = CrAr.charge0_120[CrAr.charge0_120.length - 1][0];
                        break;
                    case "Charge 1":
                        start = CrAr.charge1_120[0][0];
                        end = CrAr.charge1_120[CrAr.charge1_120.length - 1][0];
                        break;
                    case "Charge 2":
                        start = CrAr.charge2_120[0][0];
                        end = CrAr.charge2_120[CrAr.charge2_120.length - 1][0];
                        break;
                    case "Charge 3":
                        start = CrAr.charge3_120[0][0];
                        end = CrAr.charge3_120[CrAr.charge3_120.length - 1][0];
                        break;
                    case "Charge 4":
                        start = CrAr.charge4_120[0][0];
                        end = CrAr.charge4_120[CrAr.charge4_120.length - 1][0];
                        break;
                    case "Charge 5":
                        start = CrAr.charge5_120[0][0];
                        end = CrAr.charge5_120[CrAr.charge5_120.length - 1][0];
                        break;
                    case "Charge 6":
                        start = CrAr.charge6_120[0][0];
                        end = CrAr.charge6_120[CrAr.charge6_120.length - 1][0];
                        break;
                }
            } else if (caliber == 82) {
                switch (select) {
                    case "Charge 0":
                        start = CrAr.charge0_82[0][0];
                        end = CrAr.charge0_82[CrAr.charge0_82.length - 1][0];
                        break;
                    case "Charge 1":
                        start = CrAr.charge1_82[0][0];
                        end = CrAr.charge1_82[CrAr.charge1_82.length - 1][0];
                        break;
                    case "Charge 2":
                        start = CrAr.charge2_82[0][0];
                        end = CrAr.charge2_82[CrAr.charge2_82.length - 1][0];
                        break;
                }
            } else {
                switch (select) {
                    case "Charge 0":
                        start = CrAr.charge0_30[0][0];
                        end = CrAr.charge0_30[CrAr.charge0_30.length - 1][0];
                        break;
                    case "Charge 1":
                        start = CrAr.charge1_30[0][0];
                        end = CrAr.charge1_30[CrAr.charge1_30.length - 1][0];
                        break;
                    case "Charge 2":
                        start = CrAr.charge2_30[0][0];
                        end = CrAr.charge2_30[CrAr.charge2_30.length - 1][0];
                        break;
                }
            }
        }
        if (range == 0) {
            return false;
        }
        return start <= range && range <= end;
    }

    /**
     * Задает значения поправки
     *
     * @param select  выбранный charge
     * @param caliber выбрнный калибр
     * @param range   удаление цели
     */
    static private void setNDE(String select, int caliber, int range) {
        int nearby;
        if (caliber == 120) {
            switch (select) {
                case "Charge 0":
                    nearby = nearest(CrAr.charge0_120, range);
                    elev = CrAr.charge0_120[nearby][1];
                    dper100 = CrAr.charge0_120[nearby][2];
                    sel_charge = CrAr.charge0_120;
                    break;
                case "Charge 1":
                    nearby = nearest(CrAr.charge1_120, range);
                    elev = CrAr.charge1_120[nearby][1];
                    dper100 = CrAr.charge1_120[nearby][2];
                    sel_charge = CrAr.charge1_120;
                    break;
                case "Charge 2":
                    nearby = nearest(CrAr.charge2_120, range);
                    elev = CrAr.charge2_120[nearby][1];
                    dper100 = CrAr.charge2_120[nearby][2];
                    sel_charge = CrAr.charge2_120;
                    break;
                case "Charge 3":
                    nearby = nearest(CrAr.charge3_120, range);
                    elev = CrAr.charge3_120[nearby][1];
                    dper100 = CrAr.charge3_120[nearby][2];
                    sel_charge = CrAr.charge3_120;
                    break;
                case "Charge 4":
                    nearby = nearest(CrAr.charge4_120, range);
                    elev = CrAr.charge4_120[nearby][1];
                    dper100 = CrAr.charge4_120[nearby][2];
                    sel_charge = CrAr.charge4_120;
                    break;
                case "Charge 5":
                    nearby = nearest(CrAr.charge5_120, range);
                    elev = CrAr.charge5_120[nearby][1];
                    dper100 = CrAr.charge5_120[nearby][2];
                    sel_charge = CrAr.charge5_120;
                    break;
                case "Charge 6":
                    nearby = nearest(CrAr.charge6_120, range);
                    elev = CrAr.charge6_120[nearby][1];
                    dper100 = CrAr.charge6_120[nearby][2];
                    sel_charge = CrAr.charge6_120;
                    break;
            }
        } else if (caliber == 82) {
            switch (select) {
                case "Charge 0":
                    nearby = nearest(CrAr.charge0_82, range);
                    elev = CrAr.charge0_82[nearby][1];
                    dper100 = CrAr.charge0_82[nearby][2];
                    sel_charge = CrAr.charge0_82;
                    break;
                case "Charge 1":
                    nearby = nearest(CrAr.charge1_82, range);
                    elev = CrAr.charge1_82[nearby][1];
                    dper100 = CrAr.charge1_82[nearby][2];
                    sel_charge = CrAr.charge1_82;
                    break;
                case "Charge 2":
                    nearby = nearest(CrAr.charge2_82, range);
                    elev = CrAr.charge2_82[nearby][1];
                    dper100 = CrAr.charge2_82[nearby][2];
                    sel_charge = CrAr.charge2_82;
                    break;
            }
        } else {
            switch (select) {
                case "Charge 0":
                    nearby = nearest(CrAr.charge0_30, range);
                    dper100 = CrAr.charge0_30[nearby][2];
                    elev = CrAr.charge0_30[nearby][1];
                    sel_charge = CrAr.charge0_30;
                    break;
                case "Charge 1":
                    nearby = nearest(CrAr.charge1_30, range);
                    dper100 = CrAr.charge1_30[nearby][2];
                    elev = CrAr.charge1_30[nearby][1];
                    sel_charge = CrAr.charge1_30;
                    break;
                case "Charge 2":
                    nearby = nearest(CrAr.charge2_30, range);
                    dper100 = CrAr.charge2_30[nearby][2];
                    elev = CrAr.charge2_30[nearby][1];
                    sel_charge = CrAr.charge2_30;
                    break;
            }
        }

    }

}
