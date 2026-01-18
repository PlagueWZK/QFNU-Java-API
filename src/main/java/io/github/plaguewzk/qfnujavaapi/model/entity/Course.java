package io.github.plaguewzk.qfnujavaapi.model.entity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record Course(String courseName, Weeks weeks, String section, String location, String teacher) {

    public record Weeks(List<Integer> weeks) {
        public Weeks {
            if (weeks == null) {
                throw new IllegalArgumentException("周数不能为空");
            }
            for (Integer week : weeks) {
                if (week < 1 || week > 20) {
                    throw new IllegalArgumentException("周数必须在1-20之间");
                }
            }
            weeks.sort(Integer::compareTo);
        }

        public Weeks(Integer... integers) {
            this(new ArrayList<>(List.of(integers)));
        }

        public Weeks parse(String weeksStr) {
            if (weeksStr == null) {
                throw new IllegalArgumentException("周数不能为空");
            }
            List<Integer> weeksList = new ArrayList<>();
            String[] weeks = weeksStr.split(",");
            for (String week : weeks) {
                if (week.contains("-")) {
                    String[] range = week.split("-");
                    int start = Integer.parseInt(range[0]);
                    int end = Integer.parseInt(range[1]);
                    for (int i = start; i <= end; i++) {
                        weeksList.add(i);
                    }
                } else {
                    weeksList.add(Integer.parseInt(week));
                }
            }
            return new Weeks(weeksList);
        }

        private String[] normalize() {
            if (weeks.isEmpty()) {
                return new String[0];
            }
            StringBuilder builder = new StringBuilder();

            int startWeek = weeks.get(0);
            int endWeek = startWeek;

            if (weeks.size() == 1) {
                return new String[]{startWeek + "-" + endWeek};
            }

            for (int i = 1, len = weeks.size(); i < len; i++) {
                int week = weeks.get(i);
                if (week - endWeek > 1) {
                    builder.append(startWeek).append("-").append(endWeek).append("%");
                    startWeek = week;
                }
                endWeek = week;
                if (i == len - 1) {
                    builder.append(startWeek).append("-").append(endWeek);
                }
            }
            return builder.toString().split("%");
        }

        @NotNull
        @Override
        public String toString() {
            return String.join(",", normalize());
        }
    }
}
