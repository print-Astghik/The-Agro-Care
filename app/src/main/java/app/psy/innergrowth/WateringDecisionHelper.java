package app.psy.innergrowth;

public class WateringDecisionHelper {
    public static String shouldWater(double temperature, int humidity, String weatherCondition) {
        // Եթե անձրև է գալիս, ապա ջրել պետք չէ
        if (weatherCondition.toLowerCase().contains("rain")) {
            return "No need to water! 🌧️ It's raining.";
        }
        // Եթե խոնավությունը բարձր է (ավելի քան 80%), ապա նույնպես ջրել պետք չէ
        if (humidity > 80) {
            return "No need to water! 🌫️ High humidity.";
        }
        // Եթե շատ շոգ է և խոնավությունը ցածր է, ապա անհրաժեշտ է ջրել
        if (temperature > 30 && humidity < 40) {
            return "Water the apple tree! 🌡️ It's too hot and dry.";
        }
        // Եթե ջերմաստիճանը չափավոր է, բայց խոնավությունը ցածր է, կարելի է ջրել
        if (temperature > 15 && humidity < 50) {
            return "Consider watering! 🌱 Soil might be dry.";
        }
        // Այլ դեպքերում, ջրել պետք չէ
        return "No watering needed! 🍏 The conditions are good.";
    }
}
