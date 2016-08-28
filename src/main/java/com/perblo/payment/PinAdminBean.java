/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.payment;


import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.listener.HostelEntityManagerListener;
import com.perblo.payment.entity.PaymentTransaction;
import com.perblo.payment.entity.Pin;
import static com.perblo.security.LoginManager.LOGIN_USER;
import com.perblo.security.LoginUser;
import com.perblo.security.LoginUserBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.primefaces.model.UploadedFile;
import org.springframework.security.access.annotation.Secured;


/**
 *
 * @author chinedu
 */
//@Restrict("#{s:hasRole('Pin Admin') or s:hasRole('Hostel Admin')}")
@ManagedBean(name="pinAdminBean")
@SessionScoped
public class PinAdminBean implements Serializable {
    private static final Logger log = Logger.getLogger(PinAdminBean.class);
    private EntityManager entityManager;
               
    @ManagedProperty(value="#{paymentHelper}")
    private PaymentHelper paymentHelper;
    
    @ManagedProperty(value="#{loginUserBean}")
    LoginUserBean loginUserBean;
        
    List<Pin> pinList = null;
    
    private Pin pin;    
    private String serialNumber;
        
    private byte[] data;     
    private String fileExtension;    
    private String fileName;    
    private String contentType;
    
    private UploadedFile uploadedFile;
    
        
    public PinAdminBean() {
        this.entityManager = HostelEntityManagerListener.createEntityManager();
    }
                    
    @Secured("Hostel Admin")
    public void checkPinStatus() {
        try {
            Query query = entityManager.createNamedQuery("getPinBySerialNumber");
            query.setParameter(1, serialNumber);
            pin = (Pin)query.getSingleResult();
            
            
        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Could not get pin information for serial number " + serialNumber,""));
            
            log.error("Exception in checkPinStatus: " + e.getMessage());
        }
    }
    
    //@Restrict("#{s:hasRole('Pin Admin')}")
    @Secured("Pin Admin")
    public void uploadFile() {
        log.info("uploadFile");
    	pinList = new ArrayList<Pin>();
        log.info("file content size: " + uploadedFile.getContents().length);
        log.info("content type: " + uploadedFile.getContentType());
        log.info("file name: " + uploadedFile.getFileName());
                
        //log.info("filename = " + fileName + " contentType = " + contentType + " data size = " + data.length);
        
    	if(uploadedFile.getFileName().contains("xlsx") || uploadedFile.getFileName().contains("xls")) {
            getPinData();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, pinList.size() + " pin numbers uploaded",""));
            
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please upload an excel file.",""));            
        }
    	
    }
    
    private void getPinData() {
                
        String batchNumber;
        String serialNumber;        
        String pinNumber;
        float pinValue;
                
        
        log.info("getPinData()");
        int numberOfRecords = 0;

        try {            
                            
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                
                //check for xlsx files
                if (uploadedFile.getFileName().contains("xlsx")) {
                    //XSSFWorkbook workBook = new XSSFWorkbook(fistream);
                    XSSFWorkbook workBook = new XSSFWorkbook(uploadedFile.getInputstream());
                    XSSFSheet workSheet = workBook.getSheetAt(0);
                    Iterator<Row> rowsIter = workSheet.rowIterator();
                    while (rowsIter.hasNext()) {
                        Row currentRow = rowsIter.next();
                        if(currentRow.getRowNum() == 0) {
                            continue;
                        }
                        Iterator<Cell> cellsIter = currentRow.cellIterator();
                        //excel sheet format batchnumber, serial number, pin number, pin value
                        while (cellsIter.hasNext()) {
                            Cell batchNumberCell = cellsIter.next();
                            batchNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                            batchNumber = batchNumberCell.getStringCellValue();
                            
                            Cell serialNumberCell = cellsIter.next();
                            serialNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                            serialNumber = serialNumberCell.getStringCellValue();
                            
                            Cell pinNumberCell = cellsIter.next();
                            pinNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                            pinNumber = pinNumberCell.getStringCellValue();
                            
                            Cell pinValueCell = cellsIter.next();
                            pinValueCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            pinValue = new Double(pinValueCell.getNumericCellValue()).floatValue();                            
                                                       
                            numberOfRecords++;
                            
                            log.info("batchNumber = " + batchNumber + ", serialNumber = " + serialNumber + " pinValue = " + pinValue);
                            
                            Pin newpin = this.addPin(batchNumber, serialNumber, pinNumber, pinValue);
                            pinList.add(newpin);
                            
                        }
                        
                    }

                } else {
                    HSSFWorkbook hssfWorkBook = new HSSFWorkbook(uploadedFile.getInputstream());
                    HSSFSheet hssfWorkSheet = hssfWorkBook.getSheetAt(0);
                    Iterator<Row> rowsIter = hssfWorkSheet.rowIterator();
                    while (rowsIter.hasNext()) {
                        Row currentRow = rowsIter.next();
                        if(currentRow.getRowNum() == 0) {
                            continue;
                        }
                        
                        Iterator<Cell> cellsIter = currentRow.cellIterator();
                        while (cellsIter.hasNext()) {
                            Cell batchNumberCell = cellsIter.next();
                            batchNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                            batchNumber = batchNumberCell.getStringCellValue();
                            
                            Cell serialNumberCell = cellsIter.next();
                            serialNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                            serialNumber = serialNumberCell.getStringCellValue();
                            
                            Cell pinNumberCell = cellsIter.next();
                            pinNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                            pinNumber = pinNumberCell.getStringCellValue();
                            
                            Cell pinValueCell = cellsIter.next();
                            pinValueCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            pinValue = new Double(pinValueCell.getNumericCellValue()).floatValue();  
                                                                                    
                            numberOfRecords++;
                            
                            log.info("batchNumber = " + batchNumber + ", serialNumber = " + serialNumber + " pinValue = " + pinValue);
                            
                            Pin newpin = this.addPin(batchNumber, serialNumber, pinNumber, pinValue);
                            pinList.add(newpin);
                        }
                    }
                }
                              
                transaction.commit();

        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error occured while processing your request.","")); 
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error occured while processing your request.","")); 
            
        } finally {
                  
        }
        
    }
    
    private Pin addPin(String batchNumber, String serialNumber, String pinNumber, float pinValue) {
        Pin pin = new Pin();
        try {
            pin.setBatchNumber(batchNumber);
            pin.setSerialNumber(serialNumber);
            pin.setPinNumber(pinNumber);
            pin.setPinValue(pinValue);
            pin.setEnabled(true);
            pin.setUsedStatus(false);
            pin.setGenerationDate(Calendar.getInstance().getTime());
            pin.setDateUploaded(Calendar.getInstance().getTime());
            pin.setUploadedBy(loginUserBean.getCurrentUser().getUserName());
            
            //check if pin exists
            Pin existingPin = paymentHelper.getPinByPinNumber(pinNumber);
            if(existingPin == null) {                
                entityManager.persist(pin);
            }
            
        } catch(Exception e) {
            log.error("Error addPin: " + e.getMessage());
        }
        
        return pin;
    }
    
    public String getPinUser(String pinNumber) {
        String pinUser = null;
        try {
            Query query = entityManager.createNamedQuery("getPaymentTransactionByTransactionId");
            query.setParameter(1, pinNumber);
            PaymentTransaction paymentTx = (PaymentTransaction) query.getSingleResult();
            if(paymentTx == null) {
                pinUser = "Not found";
            } else {
                query = entityManager.createNamedQuery("getHostelApplicationByPaymentTransactionId");
                query.setParameter(1, paymentTx.getId());
                HostelApplication hostelApp = (HostelApplication) query.getSingleResult();
                if(hostelApp != null) {
                    pinUser = hostelApp.getStudentNumber();
                }                
            }
            
        } catch(Exception e) {
            log.error("getPinUser exception: " + e.getMessage());
        }
        
        return pinUser;
    }
          

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Pin getPin() {
        return pin;
    }

    public void setPin(Pin pin) {
        this.pin = pin;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LoginUserBean getLoginUserBean() {
        return loginUserBean;
    }

    public void setLoginUserBean(LoginUserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }
    
    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public PaymentHelper getPaymentHelper() {
        return paymentHelper;
    }

    public void setPaymentHelper(PaymentHelper paymentHelper) {
        this.paymentHelper = paymentHelper;
    }
        
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;        
    }
    
}
