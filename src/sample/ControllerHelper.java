package sample;

import javafx.scene.control.Alert;
import javafx.scene.paint.Color;

import java.util.*;

public  class ControllerHelper {
    private List<Color> usedColors = new ArrayList<>();
    Random random = new Random();

    public static void InvalidInputError(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("Invalid input");
        alert.setContentText("Not a number, or too big number");

        alert.showAndWait();
    }

    public  boolean IsNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public  int GetRandomInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public Color GetRandomColor()
    {
        Color newColor= Color.rgb(GetRandomInt(0,255),GetRandomInt(0,255),GetRandomInt(0,255));
        if(usedColors!=null)
        {
            while (usedColors.contains(newColor))
            {
                newColor= Color.rgb(GetRandomInt(0,255),GetRandomInt(0,255),GetRandomInt(0,255));
            }
        }
        usedColors.add(newColor);
        return newColor;
    }

    public int mode(int[] array) {
        int mode = array[0];
        int modeIndex=0;
        int maxCount = 0;
        for (int i = 0; i < array.length; i++) {
            int value = array[i];
            if(value==-1)
                continue;
            int count = 1;
            for (int j = 0; j < array.length; j++) {
                if (array[j] == value) count++;
                if (count > maxCount) {
                    mode = value;
                    modeIndex=i;
                    maxCount = count;
                }
            }
        }
        return modeIndex;
    }

    public Seed GetSeedByXY(List<Seed> ziarna, int x, int y)
    {
        for (Seed ziarno: ziarna
             ) {
            if(ziarno.x == x && ziarno.y == y)
            {
                return ziarno;
            }

        }
        return null;
    }

    public Color GetRandomRedColor()
    {
        int value= GetRandomInt(50,255);
        Color newColor= Color.rgb(value, 0,0);
//        if(usedColors!=null)
//        {
//            while (usedColors.contains(newColor))
//            {
//                value= GetRandomInt(0,255);
//                newColor= Color.rgb(255, value,value);
//            }
//        }
//        usedColors.add(newColor);
        return newColor;
    }

}
