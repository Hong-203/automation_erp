package com.automation_erp.framework.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InboundPage extends BasePage {

    
    private final By createInboundBtn = By.id("btn-create-inbound");
    private final By warehouseSelect = By.id("select-warehouse");
    private final By addProductBtn = By.id("btn-add-product");
    private final By skuInput = By.cssSelector(".input-sku");
    private final By qtyInput = By.cssSelector(".input-qty");
    private final By priceInput = By.cssSelector(".input-price");
    private final By saveDraftBtn = By.id("btn-save-draft");
    private final By submitBtn = By.id("btn-submit-inbound");
    private final By approveBtn = By.id("btn-approve-inbound");
    private final By confirmReceiptBtn = By.id("btn-confirm-receipt");
    private final By statusText = By.id("inbound-status");

    public InboundPage(WebDriver driver) {
        super(driver);
    }

    public void clickCreateInbound() {
        click(createInboundBtn);
    }

    public void selectWarehouse(String warehouseCode) {
        writeText(warehouseSelect, warehouseCode);
    }

    public void addProductLine(String sku, int qty, double price) {
        click(addProductBtn);
        writeText(skuInput, sku);
        writeText(qtyInput, String.valueOf(qty));
        writeText(priceInput, String.valueOf(price));
    }

    public void saveDraft() {
        click(saveDraftBtn);
    }

    public void submitForApproval() {
        click(submitBtn);
    }

    public void approveInbound() {
        click(approveBtn);
    }

    public void confirmReceipt() {
        click(confirmReceiptBtn);
    }

    public String getInboundStatus() {
        return readText(statusText);
    }
}
