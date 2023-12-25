package xyz.atsumeru.ksk2atsu.utils;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class ProgressBarBuilder {

    /**
     * Helper method for creating and configuring {@link ProgressBar} with {@link ProgressBarStyle#ASCII} style
     *
     * @param message message for progress indicating
     * @param max     maximum of progress steps
     * @return configured {@link ProgressBar} instance
     */
    public static ProgressBar create(String message, int max) {
        return new me.tongfei.progressbar.ProgressBarBuilder()
                .setTaskName(message)
                .setInitialMax(max)
                .setStyle(ProgressBarStyle.ASCII)
                .build();
    }
}
