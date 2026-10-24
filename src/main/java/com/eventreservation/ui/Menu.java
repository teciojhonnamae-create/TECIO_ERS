package com.yourdomain.ui;

import com.yourdomain.dao.UserDao;

import java.util.Scanner;

public class Menu {

    private final Authentication authentication;

    public Menu(UserDao userDao, Scanner scanner) {
        this.authentication = new Authentication(
                userDao,
                scanner
        );
    }

    public void start() {
        authentication.start();
    }
}