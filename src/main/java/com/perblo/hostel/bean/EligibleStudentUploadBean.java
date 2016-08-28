/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.bean;

import com.perblo.hostel.entity.EligibleStudent;
import com.perblo.hostel.entity.Hostel;
import com.perblo.hostel.entity.HostelAllocation;
import com.perblo.hostel.entity.HostelRoom;
import com.perblo.hostel.entity.HostelRoomBedSpace;
import com.perblo.hostel.entity.HostelStudentType;
import com.perblo.hostel.helper.HostelSettingsHelper;
import com.perblo.hostel.listener.HostelEntityManagerListener;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
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

@ManagedBean(name="eligibleStudentUploadBean")
@SessionScoped
@Secured("Hostel Admin")
public class EligibleStudentUploadBean implements Serializable {
    private static final Logger log = Logger.getLogger(EligibleStudentUploadBean.class);
    private EntityManager entityManager;     
    
    @ManagedProperty(value="#{hostelSettingsHelper}")
    private HostelSettingsHelper hostelSettingsHelper;   
    
    @ManagedProperty(value="#{loginUserBean}")
    LoginUserBean loginUserBean;
    
    private byte[] data;     
    private String fileExtension;    
    private String fileName;    
    private String contentType;
        
    private int pageSize = 10;    
    private int page;        
    private String searchParam;
    private int uploadRowIndex;
    private UploadedFile uploadedFile;        
    
    private List<EligibleStudent> eligibleStudents;    
   
    private List<EligibleStudent> uploadedEligibleStudents;
    
    public EligibleStudentUploadBean() {
        this.entityManager = HostelEntityManagerListener.createEntityManager();   
        uploadedEligibleStudents = new ArrayList<EligibleStudent>();
        eligibleStudents  = new ArrayList<EligibleStudent>();
    }
           
    public void uploadFile() {
        log.info("uploadFile");
    	uploadedEligibleStudents = new ArrayList<EligibleStudent>();
        eligibleStudents  = new ArrayList<EligibleStudent>();
        searchParam = "";
        uploadRowIndex = 0;
        log.info("filename = " + uploadedFile.getFileName() + " contentType = " + 
                uploadedFile.getContentType() + " data size = " + uploadedFile.getSize());
        
    	if(uploadedFile.getFileName().contains("xlsx") || uploadedFile.getFileName().contains("xls")) {
            getEligibleStudentData();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please upload an excel file.",""));             
        }
    	
    }
    
    private void getEligibleStudentData() {
                
        String studentNumber;
        String studentName;
        String department;
        String programme;
        String yearOfStudy;
        String studentType;
        String hostel;
          
        log.info("getEligibleStudentData");
        int numberOfRecords = 0;

        try {            
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                //check for xlsx files
                if (uploadedFile.getFileName().contains("xlsx")) {
                    XSSFWorkbook workBook = new XSSFWorkbook(uploadedFile.getInputstream());
                    XSSFSheet workSheet = workBook.getSheetAt(0);
                    Iterator<Row> rowsIter = workSheet.rowIterator();
                    while (rowsIter.hasNext()) {
                        Row currentRow = rowsIter.next();
                        if(currentRow.getRowNum() == 0) {
                            continue;
                        }
                        Iterator<Cell> cellsIter = currentRow.cellIterator();
                        //excel sheet format student number, student name, department, programme of study, year of study, hostel
                        while (cellsIter.hasNext()) {
                            Cell studentNumberCell = cellsIter.next();
                            studentNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                            studentNumber = studentNumberCell.getStringCellValue();
                            
                            Cell studentNameCell = cellsIter.next();
                            studentNameCell.setCellType(Cell.CELL_TYPE_STRING);
                            studentName = studentNameCell.getStringCellValue();
                            
                            Cell departmentCell = cellsIter.next();
                            departmentCell.setCellType(Cell.CELL_TYPE_STRING);
                            department = departmentCell.getStringCellValue();
                            /*
                            Cell programmeCell = cellsIter.next();
                            programmeCell.setCellType(Cell.CELL_TYPE_STRING);
                            programme = programmeCell.getStringCellValue();
                            
                            Cell yearOfStudyCell = cellsIter.next();
                            yearOfStudyCell.setCellType(Cell.CELL_TYPE_STRING);
                            yearOfStudy = yearOfStudyCell.getStringCellValue();
                            */
                            Cell studentTypeCell = cellsIter.next();
                            studentTypeCell.setCellType(Cell.CELL_TYPE_STRING);
                            studentType = studentTypeCell.getStringCellValue();
                            
                            Cell hostelCell = cellsIter.next();
                            hostelCell.setCellType(Cell.CELL_TYPE_STRING);
                            hostel = hostelCell.getStringCellValue();
                            
                            numberOfRecords++;
                            
                            log.info("studentNumber = " + studentNumber + ", studentName = " + studentName + " department = " + department +
                                    ", studentType = " + studentType + ", hostel = " + hostel);
                            
                            EligibleStudent eliStudent = this.getEligibleStudentByStudentNumber(studentNumber);
                            if(eliStudent == null) {
                                HostelStudentType hostelStudentType = this.getHostelStudentType(studentType);
                                if(hostelStudentType == null) {
                                    log.error("hostel student type =" + studentType + " not found");
                                } else {
                                    EligibleStudent eligibleStudent = this.addEligibleStudent(studentNumber, studentName, department, 
                                            hostelStudentType, hostel);
                                    uploadedEligibleStudents.add(eligibleStudent);
                                }    
                            } else {
                                log.warn("Eligible student already exists for " + studentNumber);
                            }
                            
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
                            Cell studentNumberCell = cellsIter.next();
                            studentNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                            studentNumber = studentNumberCell.getStringCellValue();
                            
                            Cell studentNameCell = cellsIter.next();
                            studentNameCell.setCellType(Cell.CELL_TYPE_STRING);
                            studentName = studentNameCell.getStringCellValue();
                            
                            Cell departmentCell = cellsIter.next();
                            departmentCell.setCellType(Cell.CELL_TYPE_STRING);
                            department = departmentCell.getStringCellValue();
                            /*
                            Cell programmeCell = cellsIter.next();
                            programmeCell.setCellType(Cell.CELL_TYPE_STRING);
                            programme = programmeCell.getStringCellValue();
                            
                            Cell yearOfStudyCell = cellsIter.next();
                            yearOfStudyCell.setCellType(Cell.CELL_TYPE_STRING);
                            yearOfStudy = yearOfStudyCell.getStringCellValue();
                            */
                            
                            Cell studentTypeCell = cellsIter.next();
                            studentTypeCell.setCellType(Cell.CELL_TYPE_STRING);
                            studentType = studentTypeCell.getStringCellValue();
                            
                            Cell hostelCell = cellsIter.next();
                            hostelCell.setCellType(Cell.CELL_TYPE_STRING);
                            hostel = hostelCell.getStringCellValue();
                            
                            numberOfRecords++;
                            
                            log.info("studentNumber = " + studentNumber + ", studentName = " + studentName + " department = " + department +
                                    ", studentType = " + studentType + ", hostel = " + hostel);
                            
                            EligibleStudent eliStudent = this.getEligibleStudentByStudentNumber(studentNumber);
                            if(eliStudent == null) {
                                HostelStudentType hostelStudentType = this.getHostelStudentType(studentType);
                                if(hostelStudentType == null) {
                                    log.error("hostel student type =" + studentType + " not found");
                                } else {
                                    EligibleStudent eligibleStudent = this.addEligibleStudent(studentNumber, studentName, department, 
                                           hostelStudentType, hostel);
                                    uploadedEligibleStudents.add(eligibleStudent);
                                }
                            } else {
                                log.warn("Eligible student already exists for " + studentNumber);
                            }
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
    
    private EligibleStudent addEligibleStudent(String studentNumber, String studentName, String department, 
            HostelStudentType hostelStudentType, String hostel) {
        EligibleStudent eligibleStudent = new EligibleStudent();
        try {
            eligibleStudent.setStudentNumber(studentNumber);
            eligibleStudent.setFirstName(studentName);
            eligibleStudent.setDepartment(department);
            //eligibleStudent.setProgrammeOfStudy(programme);
            eligibleStudent.setHostelStudentType(hostelStudentType);
            /*
            try {
                if(yearOfStudy.trim().equalsIgnoreCase("Final Year")) {
                    eligibleStudent.setYearOfStudy(400);
                } else {
                    eligibleStudent.setYearOfStudy(Integer.valueOf(yearOfStudy));
                }
            } catch(Exception ye) {
                log.error("invalid year of study");
            }
            */
            
            eligibleStudent.setAcademicSession(hostelSettingsHelper.getCurrentAcademicSession().getSessionName());
            eligibleStudent.setDateUploaded(Calendar.getInstance().getTime());
            eligibleStudent.setUploadedBy(loginUserBean.getCurrentUser().getUserName());
            
            entityManager.persist(eligibleStudent);
            log.info("Eligible student created for " + studentNumber);
            
            String[] hostelArray = hostel.split(" ");
            String hostelName = hostelArray[0] + " " +  hostelArray[1];
            String roomNumber = hostelArray[3];
            String space = hostelArray[4] + " " +  hostelArray[5];
            log.info("hostel = " + hostel + " , room number = " + roomNumber + " , space = " + space);
            Hostel hall = getHostelByName(hostelName);
            HostelRoomBedSpace bedSpace = this.getHostelRoomBedSpaceByPosition(space);
            HostelRoom hostelRoom = this.getHostelRoomByHostelAndRoomNumber(hall, roomNumber);
            if(hostelRoom == null) {
                hostelRoom = new HostelRoom();
                hostelRoom.setHostel(hall);
                hostelRoom.setRoomNumber(roomNumber);
                hostelRoom.setNumberOfOccupants(4);
                entityManager.persist(hostelRoom);
            }
            
            HostelAllocation hostelAllocation = new HostelAllocation();
            hostelAllocation.setStudentNumber(studentNumber);
            hostelAllocation.setHostelRoomBedSpace(bedSpace);
            hostelAllocation.setHostelRoom(hostelRoom);
            hostelAllocation.setAcademicSession(hostelSettingsHelper.getCurrentAcademicSession());
            entityManager.persist(hostelAllocation);
            log.info("Hostel allocation created for " + studentNumber);
            
        } catch(Exception e) {
            log.error("Error addEligibleStudent: " + e.getMessage());
        }
        
        return eligibleStudent;
    }
    
    private HostelStudentType getHostelStudentType(String studentType) {
        HostelStudentType hostelStudentType = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelStudentTypeByStudentType");
            query.setParameter(1, studentType);
            
            hostelStudentType = (HostelStudentType)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelStudentType: " + e.getLocalizedMessage());
        }
        
        return hostelStudentType;
    }
    
    private EligibleStudent getEligibleStudentByStudentNumber(String studentNumber) {
        EligibleStudent eligibleStudent = null;
        try {
            Query query = entityManager.createNamedQuery("getEligibleStudentByStudentNumber");
            query.setParameter(1, studentNumber);
            
            eligibleStudent = (EligibleStudent)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getEligibleStudentByStudentNumber: " + e.getLocalizedMessage());
        }
        
        return eligibleStudent;
    }
    
    private HostelRoomBedSpace getHostelRoomBedSpaceByPosition(String space) {
        HostelRoomBedSpace hostelRoomBedSpace = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelRoomBedSpaceByPosition");
            query.setParameter(1, space);
            
            hostelRoomBedSpace = (HostelRoomBedSpace)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelRoomBedSpaceByPosition: " + e.getLocalizedMessage());
        }
        
        return hostelRoomBedSpace;
    }
    
    private Hostel getHostelByName(String hostelName) {
        Hostel hostel = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelByName");
            query.setParameter(1, hostelName);
            
            hostel = (Hostel)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelByName: " + e.getLocalizedMessage());
        }
        
        return hostel;
    }
    
    private HostelRoom getHostelRoomByHostelAndRoomNumber(Hostel hostel, String roomNumber) {
        HostelRoom hostelRoom = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelRoomByHostelAndRoomNumber");
            query.setParameter(1, hostel.getId());
            query.setParameter(2, roomNumber);
            
            hostelRoom = (HostelRoom)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelRoomByHostelAndRoomNumber: " + e.getLocalizedMessage());
        }
        
        return hostelRoom;
    }
    
    public void search() {
       uploadedEligibleStudents = new ArrayList<EligibleStudent>();
       page = 0;
       queryEligibleStudent();
    }

    public void nextPage() {
       page++;
       queryEligibleStudent();
    }
    
    public void previousPage() {
    	if(page > 0) {
    		page--;
    	}
        queryEligibleStudent();
     }
       
    private void queryEligibleStudent() {
        String queryString = "";
        Query query = null;
        if (!queryString.equalsIgnoreCase("%%")) {
            queryString = "select t from EligibleStudent t where lower(t.firstName) like '%" + searchParam +
                    "%' or lower(t.department) like '" + searchParam + "' or lower(t.studentNumber) like '" + searchParam + "'";
            query = entityManager.createQuery(queryString);
            
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "You must enter a search parameter",""));             
        }

        //eligibleStudents = query.setMaxResults(pageSize).setFirstResult(page * pageSize).getResultList();
        eligibleStudents = query.getResultList();
    }

    public boolean isNextPageAvailable() {
        return eligibleStudents != null && eligibleStudents.size() == pageSize;
    }

    public boolean isPreviousPageAvailable() {
        if (page == 0) {
            return false;
        } else {
            return true;
        }
    }

    //@Factory(value = "eligibleStudentSearchPattern", scope = ScopeType.EVENT)
    public String getSearchPattern() {
        return searchParam == null
                ? "%" : '%' + searchParam.toLowerCase().replace('*', '%') + '%';
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

    public LoginUserBean getLoginUserBean() {
        return loginUserBean;
    }

    public void setLoginUserBean(LoginUserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }

    public HostelSettingsHelper getHostelSettingsHelper() {
        return hostelSettingsHelper;
    }

    public void setHostelSettingsHelper(HostelSettingsHelper hostelSettingsHelper) {
        this.hostelSettingsHelper = hostelSettingsHelper;
    }

    public List<EligibleStudent> getEligibleStudents() {
        return eligibleStudents;
    }

    public void setEligibleStudents(List<EligibleStudent> eligibleStudents) {
        this.eligibleStudents = eligibleStudents;
    }

    public List<EligibleStudent> getUploadedEligibleStudents() {
        return uploadedEligibleStudents;
    }

    public void setUploadedEligibleStudents(List<EligibleStudent> uploadedEligibleStudents) {
        this.uploadedEligibleStudents = uploadedEligibleStudents;
    }

    
    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public int getUploadRowIndex() {
        return ++uploadRowIndex;
    }

    public void setUploadRowIndex(int uploadRowIndex) {
        this.uploadRowIndex = uploadRowIndex;
    }
       
    
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;        
    }
}
