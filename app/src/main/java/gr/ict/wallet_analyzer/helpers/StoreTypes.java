package gr.ict.wallet_analyzer.helpers;

import java.util.HashMap;

import data_class.Gradient;

public class StoreTypes {
    public static final String[] storeTypes = {
            "supermarket",
            "workshop",
            "gas station",
            "service",
            "unknown",
    };

    public static final HashMap<String, Gradient> gradientFromType = new HashMap<String, Gradient>() {{
        put(storeTypes[0], new Gradient(0xffF3BA7D, 0xff353643));
        put(storeTypes[1], new Gradient(0xff4C1AC1, 0xffC68FDD));
        put(storeTypes[2], new Gradient(0x55000000, 0x55000000));
    }};
}
