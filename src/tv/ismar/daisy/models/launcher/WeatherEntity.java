package tv.ismar.daisy.models.launcher;

/**
 * Created by <huaijiefeng@gmail.com> on 9/15/14.
 */
public class WeatherEntity {

    private String name_en;
    private Detail tomorrow;
    private String name;
    private Detail today;
    private String geoid;

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public Detail getTomorrow() {
        return tomorrow;
    }

    public void setTomorrow(Detail tomorrow) {
        this.tomorrow = tomorrow;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Detail getToday() {
        return today;
    }

    public void setToday(Detail today) {
        this.today = today;
    }

    public String getGeoid() {
        return geoid;
    }

    public void setGeoid(String geoid) {
        this.geoid = geoid;
    }

    public static class Detail {
        private String date;
        private String wind_direction;
        private String phenomenon;
        private String wind_power;
        private String temperature;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getWind_direction() {
            return wind_direction;
        }

        public void setWind_direction(String wind_direction) {
            this.wind_direction = wind_direction;
        }

        public String getPhenomenon() {
            return phenomenon;
        }

        public void setPhenomenon(String phenomenon) {
            this.phenomenon = phenomenon;
        }

        public String getWind_power() {
            return wind_power;
        }

        public void setWind_power(String wind_power) {
            this.wind_power = wind_power;
        }

        public String getTemperature() {
            return temperature;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }
    }
}
