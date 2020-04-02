package com.github.makotok.github.exporter;

import org.junit.Before;
import org.junit.Test;

public class MainTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testMain() {
        Main.main(new String[] { "-repo", "makotok/Hanappe", "-oauth", System.getenv("GITHUB_OAUTH") });
    }

}
