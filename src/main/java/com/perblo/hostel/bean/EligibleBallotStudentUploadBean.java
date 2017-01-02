/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.bean;

import com.perblo.hostel.entity.*;
import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.hostel.service.HostelBallotService;
import com.perblo.hostel.service.HostelConfig;
import com.perblo.hostel.service.HostelSettingsService;
import com.perblo.security.LoginUserBean;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.Query;
import javax.servlet.http.Part;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author chinedu
 */

@ManagedBean(name="eligibleBallotUploadBean")
@SessionScoped
@Secured("Hostel Admin")
public class EligibleBallotStudentUploadBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(EligibleBallotStudentUploadBean.class);

    @ManagedProperty(value = "#{hostelEntityManager}")
    HostelEntityManager hostelEntityManager;

    @ManagedProperty(value="#{hostelSettingsService}")
    private HostelSettingsService hostelSettingsService;

    @ManagedProperty(value="#{loginUserBean}")
    LoginUserBean loginUserBean;

    @ManagedProperty(value="#{hostelBallotService}")
    private HostelBallotService hostelBallotService;
                
    private byte[] data;     
    private String fileExtension;    
    private String fileName;    
    private String contentType;
        
    private int pageSize = 10;    
    private int page;        
    private String searchParam;
        
    private int uploadRowIndex;
    private Part uploadedEligibleFile;
    private List<EligibleBallotStudent> eligibleBallotStudents;        
    private List<EligibleBallotStudent> uploadedEligibleBallotStudents;
    
    public EligibleBallotStudentUploadBean() {

    }
        
    public void uploadFile() {
        log.info("uploadFile");
    	uploadedEligibleBallotStudents = new ArrayList<EligibleBallotStudent>();
        eligibleBallotStudents  = new ArrayList<EligibleBallotStudent>();
        searchParam = "";
        uploadRowIndex = 0;

        String fileName = getFilename(uploadedEligibleFile);
        log.info("filename: " + fileName + " , contentType: " + uploadedEligibleFile.getContentType() + " , size: " + uploadedEligibleFile.getSize());

    	if(fileName.contains("xlsx") || fileName.contains("xls")) {
            getEligibleBallotStudentData(fileName);
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please upload an excel file.",""));             
        }
            	
    }
    
    private void getEligibleBallotStudentData(String fileName) {
                
        String studentNumber;
        String studentName;
        String department;
        String gender;
        log.info("getEligibleBallotStudentData");
        int numberOfRecords = 0;

        try {

            //check for xlsx files
            if (fileName.contains("xlsx")) {
                XSSFWorkbook workBook = new XSSFWorkbook(uploadedEligibleFile.getInputStream());
                XSSFSheet workSheet = workBook.getSheetAt(0);
                int noOfRows = workSheet.getPhysicalNumberOfRows();
                log.info("no of rows " + workSheet.getPhysicalNumberOfRows());

                for(int i = 1; i < noOfRows; i++) {
                    Row row = workSheet.getRow(i);
                    int noOfCells = row.getPhysicalNumberOfCells();
                    log.info("upload row " + i + " no of cells " + noOfCells);

                    Cell studentNumberCell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
                    studentNumber = studentNumberCell.getStringCellValue().trim().toUpperCase();

                    Cell studentNameCell = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
                    studentName = studentNameCell.getStringCellValue().trim();

                    Cell departmentCell = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
                    department = departmentCell.getStringCellValue().trim().toUpperCase();

                    Cell genderCell = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
                    gender = genderCell.getStringCellValue().trim().toUpperCase();

                    numberOfRecords++;

                    log.info("studentNumber = " + studentNumber + ", studentName = " + studentName + " department = " + department);

                    EligibleBallotStudent eliStudent = hostelBallotService.getEligibleBallotStudentByStudentNumber(studentNumber);
                    if(eliStudent == null) {
                        Department departmentObj = hostelBallotService.getDepartmentByName(department);
                        if(departmentObj == null) {
                            log.error("Department =" + departmentObj.getName() + " not found");
                        } else {
                            EligibleBallotStudent eligibleBallotStudent = hostelBallotService.addEligibleBallotStudent(
                                    studentNumber, studentName, departmentObj, gender, loginUserBean.getCurrentUser().getUserName());
                            uploadedEligibleBallotStudents.add(eligibleBallotStudent);
                        }
                    } else {
                        log.warn("Eligible student already exists for " + studentNumber);
                    }
                }

            } else {
                HSSFWorkbook hssfWorkBook = new HSSFWorkbook(uploadedEligibleFile.getInputStream());
                HSSFSheet hssfWorkSheet = hssfWorkBook.getSheetAt(0);
                int noOfRows = hssfWorkSheet.getPhysicalNumberOfRows();
                log.info("no of rows " + hssfWorkSheet.getPhysicalNumberOfRows());

                for(int i = 1; i < noOfRows; i++) {
                    Row row = hssfWorkSheet.getRow(i);
                    int noOfCells = row.getPhysicalNumberOfCells();
                    log.info("upload row " + i + " no of cells " + noOfCells);

                    Cell studentNumberCell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
                    studentNumber = studentNumberCell.getStringCellValue().trim().toUpperCase();

                    Cell studentNameCell = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
                    studentName = studentNameCell.getStringCellValue().trim();

                    Cell departmentCell = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
                    department = departmentCell.getStringCellValue().trim().toUpperCase();

                    Cell genderCell = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
                    gender = genderCell.getStringCellValue().trim().toUpperCase();

                    /*
                    for (int colId = 0; colId < noOfCells; colId++) {
                        Cell cell = row.getCell(colId, Row.RETURN_BLANK_AS_NULL);
                        if (cell != null) {
                            String cellValue = cell.getStringCellValue();
                            log.info("upload row " + i + " cell " + colId + " " + cellValue);
                        }
                    }
                    */

                    numberOfRecords++;

                    log.info("studentNumber = " + studentNumber + ", studentName = " + studentName + " department = " + department);

                    EligibleBallotStudent eliStudent = hostelBallotService.getEligibleBallotStudentByStudentNumber(studentNumber);
                    if(eliStudent == null) {
                        Department departmentObj = hostelBallotService.getDepartmentByName(department);
                        if(departmentObj == null) {
                            log.error("Department =" + departmentObj.getName() + " not found");
                        } else {
                            EligibleBallotStudent eligibleBallotStudent = hostelBallotService.addEligibleBallotStudent(
                                    studentNumber, studentName, departmentObj, gender, loginUserBean.getCurrentUser().getUserName());
                            uploadedEligibleBallotStudents.add(eligibleBallotStudent);
                        }
                    } else {
                        log.warn("Eligible student already exists for " + studentNumber);
                    }
                }
            }                             

           
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error occured while processing your request.","")); 
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error occured while processing your request.","")); 
        } 
        
    }


    public void search() {
       uploadedEligibleBallotStudents = new ArrayList<EligibleBallotStudent>();
       page = 0;
       queryEligibleBallotStudent();
    }

    public void nextPage() {
       page++;
       queryEligibleBallotStudent();
    }
    
    public void previousPage() {
    	if(page > 0) {
    		page--;
    	}
        queryEligibleBallotStudent();
     }
       
    private void queryEligibleBallotStudent() {
        String queryString = "";
        Query query = null;
        if (!queryString.equalsIgnoreCase("%%")) {
            queryString = "select t from EligibleBallotStudent t where lower(t.firstName) like '%" + searchParam + "%'"
                    + " or lower(t.department.name) like '%" + searchParam + "%' or lower(t.studentNumber) like '" + searchParam + "'";
            query = hostelEntityManager.getEntityManager().createQuery(queryString);
            
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "You must enter a search parameter.",""));            
        }

        //eligibleBallotStudents = query.setMaxResults(pageSize).setFirstResult(page * pageSize).getResultList();
        eligibleBallotStudents = query.getResultList();
    }

    public boolean isNextPageAvailable() {
        return eligibleBallotStudents != null && eligibleBallotStudents.size() == pageSize;
    }

    public boolean isPreviousPageAvailable() {
        if (page == 0) {
            return false;
        } else {
            return true;
        }
    }
   
    public String getSearchPattern() {
        return searchParam == null
                ? "%" : '%' + searchParam.toLowerCase().replace('*', '%') + '%';
    }

    public void validateExcelFile(FacesContext ctx, UIComponent comp, Object value) {
        List<FacesMessage> msgs = new ArrayList<FacesMessage>();
        Part file = (Part) value;
        String contentType = file.getContentType();
        log.info("contentType: " + contentType);

        String fileName = getFilename(file);
        if (!(fileName.contains("xls") || fileName.contains("xlsx"))) {

            msgs.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid File",
                    fileName + " is not a valid excel file"));
        }
        if (!msgs.isEmpty()) {
            throw new ValidatorException(msgs);
        }
    }

    private String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1);

            }
        }
        return null;
    }

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
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

    public HostelSettingsService getHostelSettingsService() {
        return hostelSettingsService;
    }

    public void setHostelSettingsService(HostelSettingsService hostelSettingsService) {
        this.hostelSettingsService = hostelSettingsService;
    }

    public HostelBallotService getHostelBallotService() {
        return hostelBallotService;
    }

    public void setHostelBallotService(HostelBallotService hostelBallotService) {
        this.hostelBallotService = hostelBallotService;
    }

    public HostelEntityManager getHostelEntityManager() {
        return hostelEntityManager;
    }

    public void setHostelEntityManager(HostelEntityManager hostelEntityManager) {
        this.hostelEntityManager = hostelEntityManager;
    }

    public List<EligibleBallotStudent> getEligibleBallotStudents() {
        return eligibleBallotStudents;
    }

    public void setEligibleBallotStudents(List<EligibleBallotStudent> eligibleBallotStudents) {
        this.eligibleBallotStudents = eligibleBallotStudents;
    }

    public List<EligibleBallotStudent> getUploadedEligibleBallotStudents() {
        return uploadedEligibleBallotStudents;
    }

    public void setUploadedEligibleBallotStudents(List<EligibleBallotStudent> uploadedEligibleBallotStudents) {
        this.uploadedEligibleBallotStudents = uploadedEligibleBallotStudents;
    }

    /*
    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
    */

    public Part getUploadedEligibleFile() {
        return uploadedEligibleFile;
    }

    public void setUploadedEligibleFile(Part uploadedEligibleFile) {
        this.uploadedEligibleFile = uploadedEligibleFile;
    }

    public LoginUserBean getLoginUserBean() {
        return loginUserBean;
    }

    public void setLoginUserBean(LoginUserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }

    public int getUploadRowIndex() {
        return ++uploadRowIndex;
    }

    public void setUploadRowIndex(int uploadRowIndex) {
        this.uploadRowIndex = uploadRowIndex;
    }


}
