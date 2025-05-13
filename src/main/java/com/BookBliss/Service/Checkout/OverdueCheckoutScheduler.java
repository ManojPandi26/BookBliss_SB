package com.BookBliss.Service.Checkout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OverdueCheckoutScheduler {

    private final CheckoutServiceImpl checkoutService;

    /**
     * Scheduled task to check for overdue checkouts and update their status
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkOverdueCheckouts() {
        log.info("Running scheduled task to check for overdue checkouts");
        try {
            checkoutService.updateOverdueCheckouts();
            log.info("Overdue checkout status update completed successfully");
        } catch (Exception e) {
            log.error("Error occurred while updating overdue checkouts", e);
        }
    }
}
