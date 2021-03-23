import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {
    Button button;
    Button clearButton;
    String formattedinput = "";

    final String pS = "<ul:ul>",
                 pE = "</ul:ul>\n",
                 psTitle = "<fp:ps title=",
                 psTitleM ="\">",
                 psTitleE = "</fp:ps>",
                 webS ="<ul:externalLink href=\"",
                 webM = "\">",
                 webE = "</ul:externalLink>";

    final String  ulHead = "<ul:customList type=\"ul\">",
            ulFoot = "</ul:customList>\n",
            ulElementS = "<ul:customListElement>",
            ulElementE ="</ul:customListElement>\n";

    final String int_curS = "<ul:format value=\"${",
                 int_curE = "}\" type=\"cost\" unit=\"true\"/>",
                 d_cur = "<ul:format value=\"${",
                 d_curM = "}\" type=\"currency\" decimals=\"",
                 d_curE = "\" unit=\"true\"/>",
                 percentageS = "<ul:format value=\"${",
                 percentageE = "}\" type=\"percentage\" unit=\"true\"/>",
                 companyName = "<ul:format value=\"${user.companyName}\"/>";

    TextArea text = new TextArea();
    TextArea output = new TextArea();
    ArrayList<String> concatToCustomList = new ArrayList<String>();
    ArrayList<String> concatToPlainText = new ArrayList<String>();


    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Plain text to JSTP/JSTL converter");

        button = new Button("Convert");
        clearButton = new Button("Clear");

        output.setEditable(false);

        output.setPrefHeight(300);

        button.setOnAction(e -> splitWords(text, text.getText(), output));
        clearButton.setOnAction(e -> clearText());

        VBox vbox = new VBox(10);

        text.setStyle("-fx-background-color: black");
        Scene scene = new Scene(vbox, 800, 500);

        vbox.setPadding(new Insets(20,20,20,20));
        vbox.getChildren().addAll(text,output, button, clearButton);

        setStartRules();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setStartRules() {
        text.setText("Rules: \n(1) Sentence must start with Big letter and end with a dot (except if it is company name)." +
                            "\n(2) Word ending with a dot + Carriage return will render in viewport as carriage return.");
    }

    private boolean b_singlecharIsNum(char c) {
        try { int cast = Character.getNumericValue(c); return true; }
        catch(NumberFormatException ex) { return false; }
    }

    private boolean b_isInt() {
        try {
            String temp = text.getText();
            Integer.parseInt(temp);
            return true;
        } catch (NumberFormatException e) { return false; }
    }
    private boolean b_isInt(String temp) {
        try {
            if(temp.endsWith(".")) {
               temp = temp.replace(".", "");
            }
            Integer.parseInt(temp);
            return true;
        } catch (NumberFormatException e) { return false; }
    }

    private boolean b_isDouble(String temp) {
        try {
            if(temp.endsWith(".")) {
                temp = temp.substring(0, temp.length()-1);
            }
            double num = Double.parseDouble(temp);
            return true;
        } catch (NumberFormatException e) { return false; }
    }

    private boolean isString() {
        if(text.getText() instanceof String) {  return true; }
        else { return false; }
    }

  

    private void loopThroughInput (TextArea inp, String message, TextArea output) {
        b_isInt();
        String rawText = inp.getText();
        String[] listOfWords = rawText.split("\\r?\\n?\\s+");
        String temp = "";
        String currentElement = null;
        String temporarySentence = "";

            for (int i = 0; i < listOfWords.length; i++) {
                currentElement = listOfWords[i];
                System.out.println(currentElement);
                 if(isString()) {
                    if(Character.isUpperCase(listOfWords[0].charAt(0))) {
                        temporarySentence = pS + listOfWords[0];
                    }
                    if(Character.isUpperCase(listOfWords[i].charAt(0))) {
                       temp += pS;
                    }
                    //Replace lower character with bigger character in beg of sentence
                       else if(listOfWords[i] == listOfWords[0] && Character.isLowerCase(listOfWords[i].charAt(0))) {
                          listOfWords[i] =
                          listOfWords[i].replace(listOfWords[i].charAt(0),
                          Character.toUpperCase(listOfWords[i].charAt(0)));
                          System.out.print("toUpperCase" + listOfWords[i]);

                        }
                    temp = temp + " " + listOfWords[i];

                    if(listOfWords[i].endsWith(".") && !b_isInt(listOfWords[i]) && !b_isList(listOfWords[i])) {
                       temp += pE;
                    }

                    if(b_isList(listOfWords[i])) {
                        String tempFormat = "";
                        for(int j = i; j < listOfWords.length; j++) {
                           listOfWords[j] = WrapWordWithFormat(listOfWords[j]);
                           if(!listOfWords[j].endsWith(".")) {
                                listOfWords[j] = removeBullets(listOfWords[j]);
                                tempFormat = tempFormat.trim() + " " + listOfWords[j];
                            }
                            if(listOfWords[j].endsWith(".")) {
                                tempFormat = tempFormat + " " + listOfWords[j];
                                concatToCustomList.add(tempFormat); //Add processed input as a sentence into our list.
                                tempFormat = ""; //Clean temp for next element
                                break; //Jump to process & add next list element.
                            }
                        }
                        printListToOutput();
                    }
                }
            }
            formattedinput = temporarySentence + "\n"+ formattedinput;
            output.setText(formattedinput);
    }

    private void printListToOutput() {
        formattedinput = ulHead + "\n";
        for(int o = 0; o < concatToCustomList.size(); o++) {
            formattedinput = formattedinput + " " + ulElementS  + " " + concatToCustomList.get(o) +" " + ulElementE;
        }
        formattedinput = formattedinput + " " + ulFoot;
    }

    private boolean b_isList(String inp) {
        if(inp.startsWith("-") || inp.startsWith("•")) {
            return true;
        }
        return false;
    }
    private boolean b_isWebsite(String inp) {
        if(inp.startsWith("https") || inp.startsWith("http") || inp.startsWith("www.")) {
            return true;
        }
        return false;
    }
    private String tagAsWebsite(String inp) {
        String websiteName = inp;
        String link = inp;
        if(inp.startsWith("www")) {
            websiteName = websiteName.replaceAll("www.", "https://");
            link = websiteName;

        }
        if(link.startsWith("https://")) {
            websiteName = websiteName.replaceAll(".*//|/.*", "");
        }
        if(link.endsWith(".")) {
            //We cannot have a dot in the link, therefor we remove it and place it on the end of the sentence.
            link = link.substring(0, link.length() -1);
            return webS + link + webM + websiteName + webE + ".";
        }
        else {
            return webS + link + webM + websiteName + webE;
        }
    }
    private boolean b_isPercentage(String inp) {
        if(inp.endsWith("%")) {
            return true;
        }
        return false;
    }
    private boolean b_isCurrency(String inp) {
        if(inp.contains("€") || inp.contains("£")) { // Could also be replaced with a list that contains all currency-related symbols.
            return true;
        }
        else { return false; }
    }
    private String tagAsPercentage(String inp) {
        inp = inp.replace('%', '\0');
        return percentageS + inp + percentageE;
    }
    private boolean b_isDecimalCurrency(String inp) {
    try {
        double testIfDouble = Double.parseDouble(inp);
        return true;
    } catch(NumberFormatException ex) { return false; }
    }
    private String tagAsIntCurrency(String inp) {
        if(inp.contains("€")) {
            inp = inp.replaceAll("€", "");
        }
        return int_curS + inp + int_curE;
    }

    private String tagAsNumber(String inp) {
        return "";
    }

    private String tagAsDecimalCurrency(String inp) {
    int amount_of_decimals = 0;
    //Count the amount of numbers after decimal point.
    for(int i = 0; i < inp.length(); i++) {
        if(inp.charAt(i) == '.') {
            for(int j = i; j < inp.length() -1; j++) {
                if(b_singlecharIsNum(inp.charAt(j))) { amount_of_decimals++; }
            }
        }
    }
    return d_cur + inp + d_curM + amount_of_decimals + d_curE;
    }
    private boolean b_containsEURchar(String inp) {
        if(inp.toLowerCase().contains("eur") |inp.toLowerCase().contains("euro") || inp.contains("€")
        || inp.toLowerCase().contains("sek") || inp.toLowerCase().contains("kronor")) {
        return true;
        }
        return false;
    }
    private String removeEURChar(String inp) {
        inp = inp.replaceAll("EUR", "");
        inp = inp.toLowerCase().replaceAll("kronor", "");
        inp = inp.toLowerCase().replaceAll("sek", "");
        return inp;
    }
    private String removeBullets(String inp) {
        if(inp.contains("-") || inp.contains("•")) {
            inp = inp.replace('-', '\0');
            inp = inp.replace('•', '\0');
        }
        return inp;
    }
    private String WrapWordWithFormat(String inp) {
        if(b_isWebsite(inp)) {
            inp = tagAsWebsite(inp);
        }
        if(b_isPercentage(inp)){
            inp = tagAsPercentage(inp);
        }
        if(b_containsEURchar(inp) || b_isCurrency(inp)) {
            inp = tagAsIntCurrency(inp);
        }
        if(b_isDecimalCurrency(inp)) {
            inp = tagAsDecimalCurrency(inp);
        }
        if(b_containsEURchar(inp)) {
            inp = removeEURChar(inp);
        }
        if(b_isInt(inp)) {
            inp = tagAsNumber(inp);
        }
        return inp;
    }


    private boolean isReturnCarriage(String inp, TextArea output) {
        if(inp.contains("\n")) { return true; }
        else {
            System.out.println("Not carriage return");
        return false;
        }
    }

    void splitWords(TextArea inp, String message, TextArea output) {
        loopThroughInput(inp, message, output);
        output.getText();
    }

    void clearText() {
        concatToCustomList.removeAll(concatToCustomList);
        text.setText("");
        output.setText("");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

