package gr.ict.wallet_analyzer.helpers;

import data_class.Gradient;

public class StoreTypeFinder {

    public static Gradient findGradient(String type) {
        Gradient gradient = StoreTypes.gradientFromType.get(type.toLowerCase());

        if (gradient == null) {
            gradient = new Gradient(0x00ffffff, 0x00ffffff);
        }
        return gradient;
    }
}
