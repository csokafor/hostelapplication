/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.service;

import com.perblo.hostel.entity.Hostel;
import com.perblo.hostel.entity.HostelAllocation;
import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entity.HostelRoom;
import com.perblo.hostel.entity.HostelRoomBedSpace;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;


import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author chinedu
 */

@Service(value="hostelRoomAllocationService")
@Transactional(value = "hostelTxManager", rollbackFor = Exception.class)
public class HostelRoomAllocationService implements Serializable {
    
    public static final String UPPER_BED_RIGHT = "Upper Bed Right";
    public static final String UPPER_BED_LEFT = "Upper Bed Left";
    public static final String LOWER_BED_RIGHT = "Lower Bed Right";
    public static final String LOWER_BED_LEFT = "Lower Bed Left";
    public static final String UPPER_BED_CENTER = "Upper Bed Center";
    public static final String LOWER_BED_CENTER = "Lower Bed Center";
    
    public static final String HOSTEL_SCHOLARSHIP_MALE = "Scholarship Male";
    public static final String HOSTEL_SCHOLARSHIP_FEMALE = "Scholarship Female";
    public static final String HOSTEL_PG_MALE = "PG Male";
    public static final String HOSTEL_PG_FEMALE = "PG Female";
    public static final String HOSTEL_MEDICAL_MALE = "Medical Male";
    public static final String HOSTEL_MEDICAL_FEMALE = "Medical Female";
    public static final String HOSTEL_MALE = "Male";
    public static final String HOSTEL_FEMALE = "Female";
    private static final Logger log = LoggerFactory.getLogger(HostelRoomAllocationService.class);

    @Autowired
    HostelEntityManager hostelEntityManager;

    @ManagedProperty(value="#{hostelApplicationService}")
    private HostelApplicationService hostelApplicationService;
         
    @ManagedProperty(value="#{hostelSettingsService}")
    private HostelSettingsService hostelSettingsService;
    
    Random random = new Random();
    
    List<HostelRoomBedSpace> sixRoomBedSpace = null;
    List<HostelRoomBedSpace> fourRoomBedSpace = null;
    
    private static Map<String,ArrayList<HostelRoom>> hostelRoomMap = null;
    private static Map<String,Map<Long,Integer>> hostelRoomAllocationMap = null;
    
    private static ArrayList<HostelRoom> maleRoomMap = null;
    private static ArrayList<HostelRoom> femaleRoomMap = null;
    private static ArrayList<HostelRoom> scholarshipMaleRoomMap = null;
    private static ArrayList<HostelRoom> scholarshipFemaleRoomMap = null;
    private static ArrayList<HostelRoom> pgMaleRoomMap = null;
    private static ArrayList<HostelRoom> pgFemaleRoomMap = null;
    private static ArrayList<HostelRoom> medicalMaleRoomMap = null;
    private static ArrayList<HostelRoom> medicalFemaleRoomMap = null;
    
    private static Map<Long,Integer> maleRoomAllocationMap = null;
    private static Map<Long,Integer> femaleRoomAllocationMap = null;
    private static Map<Long,Integer> scholarshipMaleRoomAllocationMap = null;
    private static Map<Long,Integer> scholarshipFemaleRoomAllocationMap = null;
    private static Map<Long,Integer> pgMaleRoomAllocationMap = null;
    private static Map<Long,Integer> pgFemaleRoomAllocationMap = null;
    private static Map<Long,Integer> medicalMaleRoomAllocationMap = null;
    private static Map<Long,Integer> medicalFemaleRoomAllocationMap = null;
    //scholarship male and female
    //PG male and female
    //Medical male and female
    
    private static boolean hostelRoomInitialized;
    
    private final Object maleRoomLock = new Object();
    private final Object femaleRoomLock = new Object();
    private final Object hostelRoomLock = new Object();
    
    
    //hall2annex. Hall 2
    //roombedspace 4: 11,12,13,14
    //Calabar Rd
    //roombedspace 6: RM1A, 1B, RM2A, 2B, RM 3A, 3B, RM 4A, 4B
    //Moore Rd 2: DOUBLE RM
    //BlockCOM A,C,D; Block A,B,C,D: Space 1
    //Scholarship hostel: Space 1,2,3,4
    //Hall 4,6,8,9: Space 1,2,3,4

    
    public HostelRoomAllocationService() {
    }
    
    private void initializeHostelRooms() {
        
        if(!hostelRoomInitialized) {
            
            sixRoomBedSpace = this.getSixHostelRoomBedSpace();
            fourRoomBedSpace = this.getFourHostelRoomBedSpace();            
                       
            hostelRoomMap = new HashMap<String,ArrayList<HostelRoom>>();
            hostelRoomAllocationMap = new HashMap<String,Map<Long,Integer>>();

            List<Hostel> hostels = hostelApplicationService.getHostelList();
            for(Hostel hostel : hostels) {                
                this.populateHostelRoomMap(hostel, hostelRoomMap, hostelRoomAllocationMap);
            }
                          
            hostelRoomInitialized = true;
        
        }
        
        
    }
    
    private void populateHostelRoomMap(Hostel hostel, Map<String,ArrayList<HostelRoom>> hostelRoomMap, 
            Map<String,Map<Long,Integer>> hostelRoomAllocationMap) {
        
        ArrayList<HostelRoom> roomMap = new ArrayList<HostelRoom>();
        Map<Long,Integer> roomAllocationMap = new HashMap<Long, Integer>();
        Set<HostelRoom> hostelRooms = hostel.getHostelRooms();
        
        for(HostelRoom hostelRoom : hostelRooms) {
            if(hostelRoom.getHostelAllocations().size() < hostelRoom.getNumberOfOccupants()) {
                log.info("hostel: " + hostel.getHostelName() + ", hostel room: " + hostelRoom.toString());
                roomMap.add(hostelRoom);                        
                roomAllocationMap.put(hostelRoom.getId(), hostelRoom.getHostelAllocations().size());                
            }
        }
        
        if(hostel.getDescription().startsWith(HOSTEL_FEMALE)) {
            hostelRoomMap.put(HOSTEL_FEMALE, roomMap);
            hostelRoomAllocationMap.put(HOSTEL_FEMALE, roomAllocationMap); 
        } else if(hostel.getDescription().startsWith(HOSTEL_MALE)) {
            hostelRoomMap.put(HOSTEL_MALE, roomMap);
            hostelRoomAllocationMap.put(HOSTEL_MALE, roomAllocationMap);
            
        } else if(hostel.getDescription().startsWith(HOSTEL_SCHOLARSHIP_MALE)) {
            hostelRoomMap.put(HOSTEL_SCHOLARSHIP_MALE, roomMap);
            hostelRoomAllocationMap.put(HOSTEL_SCHOLARSHIP_MALE, roomAllocationMap);
        } else if(hostel.getDescription().startsWith(HOSTEL_SCHOLARSHIP_FEMALE)) {
            hostelRoomMap.put(HOSTEL_SCHOLARSHIP_FEMALE, roomMap);
            hostelRoomAllocationMap.put(HOSTEL_SCHOLARSHIP_FEMALE, roomAllocationMap);
            
        } else if(hostel.getDescription().startsWith(HOSTEL_PG_FEMALE)) {
            hostelRoomMap.put(HOSTEL_PG_FEMALE, roomMap);
            hostelRoomAllocationMap.put(HOSTEL_PG_FEMALE, roomAllocationMap); 
        } else if(hostel.getDescription().startsWith(HOSTEL_PG_MALE)) {
            hostelRoomMap.put(HOSTEL_PG_MALE, roomMap);
            hostelRoomAllocationMap.put(HOSTEL_PG_MALE, roomAllocationMap);
            
        } else if(hostel.getDescription().startsWith(HOSTEL_MEDICAL_FEMALE)) {
            hostelRoomMap.put(HOSTEL_MEDICAL_FEMALE, roomMap);
            hostelRoomAllocationMap.put(HOSTEL_MEDICAL_FEMALE, roomAllocationMap);
        } else if(hostel.getDescription().startsWith(HOSTEL_MEDICAL_MALE)) {
            hostelRoomMap.put(HOSTEL_MEDICAL_MALE, roomMap);
            hostelRoomAllocationMap.put(HOSTEL_MEDICAL_MALE, roomAllocationMap);
        }
    }
        
    public HostelAllocation getHosteRoomAllocation(HostelApplication hostelApplication) {
        HostelAllocation hostelAllocation = null;
        
        if (hostelApplication.getStudentType().getStudentType().equalsIgnoreCase(hostelApplicationService.SCHOLARSHIP)) {
            if(hostelApplication.getGender().equalsIgnoreCase("male")) {
                hostelAllocation = getRoom(HOSTEL_SCHOLARSHIP_MALE, hostelApplication);
            } else {
                hostelAllocation = getRoom(HOSTEL_SCHOLARSHIP_FEMALE, hostelApplication);
            }
        } else if (hostelApplication.getStudentType().getStudentType().equalsIgnoreCase(hostelApplicationService.POST_GRADUATE)) {
            if(hostelApplication.getGender().equalsIgnoreCase("male")) {
                hostelAllocation = getRoom(HOSTEL_PG_MALE, hostelApplication);
            } else {
                hostelAllocation = getRoom(HOSTEL_PG_FEMALE, hostelApplication);
            }
        } else if (hostelApplication.getStudentType().getStudentType().equalsIgnoreCase(hostelApplicationService.MEDICAL_STUDENT)) {
            if(hostelApplication.getGender().equalsIgnoreCase("male")) {
                hostelAllocation = getRoom(HOSTEL_MEDICAL_MALE, hostelApplication);
            } else {
                hostelAllocation = getRoom(HOSTEL_MEDICAL_FEMALE, hostelApplication);
            }
        } else {
            if(hostelApplication.getGender().equalsIgnoreCase("male")) {
                hostelAllocation = getRoom(HOSTEL_MALE, hostelApplication);
            } else {
                hostelAllocation = getRoom(HOSTEL_FEMALE, hostelApplication);
            }
        }
        
        /*
         if(hostelApplication.getGender().equalsIgnoreCase("male")) {
            hostelAllocation = getMaleRoom(hostelApplication);      
            // try three times
            if(hostelAllocation == null) {
                hostelAllocation = getMaleRoom(hostelApplication);
                if(hostelAllocation == null) {
                    hostelAllocation = getMaleRoom(hostelApplication); 
                    if(hostelAllocation == null) {
                        hostelAllocation = getMaleRoom(hostelApplication); 
                    }
                }
            }
        } else {
            hostelAllocation = getFemaleRoom(hostelApplication);
            // try three times
            if(hostelAllocation == null) {
                hostelAllocation = getFemaleRoom(hostelApplication);
                if(hostelAllocation == null) {
                    hostelAllocation = getFemaleRoom(hostelApplication); 
                    if(hostelAllocation == null) {
                        hostelAllocation = getFemaleRoom(hostelApplication); 
                    }
                }
            }
        }
        * 
        */
        /* 
        HostelRoom hostelRoom = entityManager.find(HostelRoom.class, roomId);
        hostelAllocation = new HostelAllocation();
        hostelAllocation.setHostelRoom(hostelRoom);
        hostelAllocation.setStudentNumber(hostelApplication.getStudentNumber());
        */
        return hostelAllocation;
    }
    
    private HostelAllocation getRoom(String hostelType, HostelApplication hostelApplication) {
        synchronized(hostelRoomLock) {
            HostelAllocation hostelAllocation = null;
            Long roomId = null;
            initializeHostelRooms();
            
            ArrayList<HostelRoom> roomMap = hostelRoomMap.get(hostelType);
            Map<Long,Integer> roomAllocationMap = hostelRoomAllocationMap.get(hostelType);

            Integer roomIndex = random.nextInt(maleRoomMap.size());
            HostelRoom hostelRoom = roomMap.get(roomIndex);
            roomId = hostelRoom.getId();
            log.info("allocated room Id = " + roomId);

            Integer allocationCounter = roomAllocationMap.get(roomId);
            allocationCounter++;
            if(allocationCounter > hostelRoom.getNumberOfOccupants()) {
                log.info(hostelRoom.toString() + " is now fully occupied removing it from list");
                roomMap.remove(roomIndex.intValue());
                roomAllocationMap.remove(hostelRoom.getId());
            } else {
                HostelRoomBedSpace bedSpace = this.getHostelRoomSpace(hostelRoom, hostelApplication.getYearOfStudy());            
                if(bedSpace == null) {                
                    log.info("no free bedspace in " + hostelRoom.getRoomNumber());
                } else {
                    hostelAllocation = new HostelAllocation();
                    hostelAllocation.setHostelRoom(hostelRoom);
                    hostelAllocation.setHostelRoomBedSpace(bedSpace);
                    hostelAllocation.setStudentNumber(hostelApplication.getStudentNumber());
                    maleRoomAllocationMap.put(roomId, allocationCounter);
                }

            }        

            return hostelAllocation;
        }
    }
    
    /*
    private HostelAllocation getFemaleRoom(HostelApplication hostelApplication) {
         synchronized(femaleRoomLock) {
            HostelAllocation hostelAllocation = null;
            Long roomId = null;
            initializeHostelRooms();

            Integer roomIndex = random.nextInt(femaleRoomMap.size());
            HostelRoom hostelRoom = femaleRoomMap.get(roomIndex);
            roomId = hostelRoom.getId();
            log.info("allocated room Id = " + roomId);

            Integer allocationCounter = femaleRoomAllocationMap.get(roomId);
            allocationCounter++;

            if(allocationCounter > hostelRoom.getNumberOfOccupants()) {
                log.info(hostelRoom.toString() + " is fully occupied removing it from list");
                femaleRoomMap.remove(roomIndex.intValue());
                femaleRoomAllocationMap.remove(hostelRoom.getId());
            } else {
                 HostelRoomBedSpace bedSpace = this.getHostelRoomSpace(hostelRoom, hostelApplication.getYearOfStudy());            
                if(bedSpace == null) {                
                    log.info("no free bedspace in " + hostelRoom.getRoomNumber());
                } else {
                    hostelAllocation = new HostelAllocation();
                    hostelAllocation.setHostelRoom(hostelRoom);
                    hostelAllocation.setHostelRoomBedSpace(bedSpace);
                    hostelAllocation.setStudentNumber(hostelApplication.getStudentNumber());
                    femaleRoomAllocationMap.put(roomId, allocationCounter);
                }
            }

            return hostelAllocation;
         }
    }
    
    * 
    */
    private HostelRoomBedSpace getHostelRoomSpace(HostelRoom hostelRoom, String yearOfStudy) {
        
        HostelRoomBedSpace freeSpace = null;
        boolean bedSpaceAllocated = false;
        
        if(hostelRoom.getNumberOfOccupants() <= 4) {
            log.info("hostel room 4 max occupants " + hostelRoom.getNumberOfOccupants());
            for(HostelRoomBedSpace bedSpace : this.fourRoomBedSpace) {      
                log.info("bedspace = " + bedSpace.getPosition());
                
                if(hostelRoom.getHostelAllocations().isEmpty()) {
                    if(bedSpace.getPosition().contains("Lower")) {
                        if(yearOfStudy.contains("1")) {
                            log.info(bedSpace.getPosition() + " allocate to first year");
                            freeSpace = bedSpace;
                            bedSpaceAllocated = true;
                        }
                    } else {
                        log.info(bedSpace.getPosition() + " allocate to final year");
                        freeSpace = bedSpace;
                        bedSpaceAllocated = true;
                    }
                } else {
                    for(HostelAllocation hostelAllocation : hostelRoom.getHostelAllocations()) {
                        if(bedSpace.getId() == hostelAllocation.getHostelRoomBedSpace().getId()) {
                            break;
                        } else {
                            log.info(bedSpace.getPosition() + " not allocated");

                            if(bedSpace.getPosition().contains("Lower")) {
                                if(yearOfStudy.contains("1")) {
                                    log.info(bedSpace.getPosition() + " allocate to first year");
                                    freeSpace = bedSpace;
                                    bedSpaceAllocated = true;
                                    break;
                                }
                            } else {
                                log.info(bedSpace.getPosition() + " allocate to final year");
                                freeSpace = bedSpace;
                                bedSpaceAllocated = true;
                                break;
                            }                      
                        }
                     }
                }
                if(bedSpaceAllocated) {
                    break;
                }
            }
        } else {
            log.info("hostel room six max occupants " + hostelRoom.getNumberOfOccupants());
            for(HostelRoomBedSpace bedSpace : this.sixRoomBedSpace) {
                log.info("bedspace = " + bedSpace.getPosition());                
                
                if(hostelRoom.getHostelAllocations().isEmpty()) {
                    if(bedSpace.getPosition().contains("Lower")) {
                        if(yearOfStudy.contains("1")) {
                            log.info(bedSpace.getPosition() + " allocate to first year");
                            freeSpace = bedSpace;
                            bedSpaceAllocated = true;
                        }
                    } else {
                        log.info(bedSpace.getPosition() + " allocate to final year");
                        freeSpace = bedSpace;
                        bedSpaceAllocated = true;
                    }
                } else {
                                    
                    for(HostelAllocation hostelAllocation : hostelRoom.getHostelAllocations()) {
                        if(bedSpace.getId() == hostelAllocation.getHostelRoomBedSpace().getId()) {
                            break;
                        } else {
                            log.info(bedSpace.getPosition() + " not allocated");
                            if(bedSpace.getPosition().contains("Lower")) {
                                if(yearOfStudy.contains("1")) {
                                    log.info(bedSpace.getPosition() + " allocate to first year");
                                    freeSpace = bedSpace;
                                    bedSpaceAllocated = true;
                                    break;
                                }
                            } else {
                                log.info(bedSpace.getPosition() + " allocate to final year");
                                freeSpace = bedSpace;
                                bedSpaceAllocated = true;
                                break;
                            } 
                        }
                     }
                }
                if(bedSpaceAllocated) {
                    break;
                }
            }
        }
        
        return freeSpace;
    }
    
    private List<HostelRoomBedSpace> getSixHostelRoomBedSpace() {
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllHostelRoomBedSpace");
        List<HostelRoomBedSpace> roomSpaces = (List<HostelRoomBedSpace>) query.getResultList();
        
        return roomSpaces;
    }
    
    private List<HostelRoomBedSpace> getFourHostelRoomBedSpace() {
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getFourRoomHostelRoomBedSpace");
        List<HostelRoomBedSpace> roomSpaces = (List<HostelRoomBedSpace>) query.getResultList();
        
        return roomSpaces;
    }

    public boolean isHostelRoomInitialized() {
        return hostelRoomInitialized;
    }

    public void setHostelRoomInitialized(boolean hostelRoomInitialized) {
        this.hostelRoomInitialized = hostelRoomInitialized;
    }

    public HostelApplicationService getHostelApplicationService() {
        return hostelApplicationService;
    }

    public void setHostelApplicationService(HostelApplicationService hostelApplicationService) {
        this.hostelApplicationService = hostelApplicationService;
    }

    public HostelSettingsService getHostelSettingsService() {
        return hostelSettingsService;
    }

    public void setHostelSettingsService(HostelSettingsService hostelSettingsService) {
        this.hostelSettingsService = hostelSettingsService;
    }
    

}
