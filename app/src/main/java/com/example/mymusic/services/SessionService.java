package com.example.mymusic.services;

import com.example.mymusic.utils.Screen;

import java.util.ArrayList;

public class SessionService {

    private static ArrayList<Screen> screens = new ArrayList<>();

    public static ArrayList<Screen> getScreens() {
        return screens;
    }

    public static void addScreen(Screen screen) {
        screens.add(screen);
    }

    public static void deleteScreen(int i) {
        screens.remove(i);
    }
}
