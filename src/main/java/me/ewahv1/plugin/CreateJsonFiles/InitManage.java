package me.ewahv1.plugin.CreateJsonFiles;

public class InitManage {

    public void initialize() {
        CreateBagsOfTrinketsJson.createJson();
        CreateBagOfTrinketsConfigJson.createJson();
        CreateTrinketsJson.createJson();
    }
}
