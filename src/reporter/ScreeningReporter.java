/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reporter;

import agent.MSM;
import community.Community ;
/**
 *
 * @author MichaelWalker
 */

import java.io.* ;
import java.util.ArrayList ;
import java.util.HashMap;
import java.util.logging.Level;


public class ScreeningReporter extends Reporter {

    static String INFECTED = "infected" ;
    static String SYMPTOMATIC = "symptomatic" ;
    static String TESTED = "tested" ;
    static String TREATED = "treated" ;
    
    public ScreeningReporter()
    {
        
    }
    
    public ScreeningReporter(String simname, ArrayList<String> report) {
        super(simname, report);
        // TODO Auto-generated constructor stub
    }

    public ScreeningReporter(String simName, String reportFilePath)
    {
        super(simName,reportFilePath) ;
    }
    
    /**
     * 
     * @param siteNames
     * @param backYears
     * @param lastYear
     * @return Year-by-year report for backYears years on notification on last day
     * of each year ending lastYear.
     */
    public HashMap<Object,Number[]> 
        prepareYearsNotificationsRecord(String[] siteNames, int backYears, int lastYear) 
        {
            HashMap<Object,Number[]> notificationRecordYears = new HashMap<Object,Number[]>() ;
            
            int maxCycles = getMaxCycles() ;
            
            HashMap<Object,Number> notificationsRecord ;
            for (int year = 0 ; year < backYears ; year++ )
            {
                Number[] yearlyNotificationsRecord = new Number[siteNames.length] ;
               
                //endCycle = maxCycles - year * DAYS_PER_YEAR ;
                notificationsRecord = prepareFinalNotificationsRecord(siteNames, year, 0, DAYS_PER_YEAR, maxCycles);
               
                for (int siteIndex = 0 ; siteIndex < siteNames.length ; siteIndex++ )
                    yearlyNotificationsRecord[siteIndex] = notificationsRecord.get(siteNames[siteIndex]) ;
                
                notificationRecordYears.put(lastYear - year, (Number[]) yearlyNotificationsRecord.clone()) ;
            }
            
            return notificationRecordYears ;
        }
    
    /**
     * 
     * @param siteNames
     * @return Records of final incidence for specified siteNames and in total.
     */
    public HashMap<Object,Number> prepareFinalNotificationsRecord(String[] siteNames, int backMonths, int backDays)
    {
        int endCycle = getMaxCycles() ;
        
        return prepareFinalNotificationsRecord(siteNames, 0, backMonths, backDays, endCycle) ;
    }
    
    /**
     * 
     * @param siteNames
     * @param backYears
     * @param endCycle
     * @return Records of final notifications for specified siteNames and in total.
     */
    public HashMap<Object,Number> prepareFinalNotificationsRecord(String[] siteNames, int backYears, int backMonths, int backDays, int endCycle)
    {
        HashMap<Object,Number> finalNotifications = new HashMap<Object,Number>() ;
        
        endCycle -= backYears * DAYS_PER_YEAR ;
        
        int notifications ;
        String record ;
        
        //String finalIncidenceRecord ; // getFinalRecord() ;
        ArrayList<String> finalNotificationsReport = getBackCyclesReport(0, backMonths, backDays, endCycle) ;
        
        double population = getPopulation() ; // Double.valueOf(getMetaDatum("Community.POPULATION")) ;
        // Adjust for portion of year sampled and units of 100000 person-years
        double denominator = population * getBackCycles(0,backMonths,backDays)/(DAYS_PER_YEAR * 100000) ;
        for (String siteName : siteNames)
        {
            notifications = 0 ;
            for (String finalIncidenceRecord : finalNotificationsReport)
            {
                // Count infected siteName
                record = boundedStringByContents(siteName,AGENTID,finalIncidenceRecord) ;
                
                //* 100 because units  per 100 person years
                notifications += countValueIncidence("treated","",record,0)[1] ;
                
            }
            finalNotifications.put(siteName,notifications/denominator) ;
        }
        notifications = 0 ;
        for (String finalNotificationsRecord : finalNotificationsReport)
        {
            notifications += countValueIncidence("treated","",finalNotificationsRecord,0)[1] ;
                
        }
        finalNotifications.put("all",notifications/denominator) ;
        
        return finalNotifications ;
    }
 
    /**
     * 
     * @param siteNames
     * @param backYears
     * @param lastYear
     * @return Year-by-year report for backYears years on prevalence on last day
     * of each year ending lastYear.
     */
    public HashMap<Object,Number[]> 
        prepareYearsPrevalenceRecord(String[] siteNames, int backYears, int lastYear) 
        {
            HashMap<Object,Number[]> prevalenceRecordYears = new HashMap<Object,Number[]>() ;
            
            int maxCycles = getMaxCycles() ;
            
            int endCycle ;
            HashMap<Object,Number> prevalenceRecord ;
            for (int year = 0 ; year < backYears ; year++ )
            {
                Number[] yearlyPrevalenceRecord = new Number[siteNames.length] ;
               
                endCycle = maxCycles - year * DAYS_PER_YEAR ;
                prevalenceRecord = prepareFinalPrevalencesRecord(siteNames, endCycle);
               
                for (int siteIndex = 0 ; siteIndex < siteNames.length ; siteIndex++ )
                    yearlyPrevalenceRecord[siteIndex] = prevalenceRecord.get(siteNames[siteIndex]) ;
                
                prevalenceRecordYears.put(lastYear - year, (Number[]) yearlyPrevalenceRecord.clone()) ;
            }
            
            return prevalenceRecordYears ;
        }
    
    /**
     * 
     * @param siteNames
     * @return Records of final prevalences for specified siteNames and in total.
     */
    public HashMap<Object,Number> prepareFinalPrevalencesRecord(String[] siteNames)
    {
        int endCycle = getMaxCycles() ;
        
        return prepareFinalPrevalencesRecord(siteNames, endCycle) ;
    }
    
    /**
     * 
     * @param siteNames
     * @return Records of final prevalences for specified siteNames and in total.
     */
    public HashMap<Object,Number> prepareFinalPrevalencesRecord(String[] siteNames, int endCycle)
    {
        HashMap<Object,Number> finalPrevalencesRecord = new HashMap<Object,Number>() ;
        
        int prevalence ;
        
        String finalPrevalenceRecord = getBackCyclesReport(0,0,1,endCycle).get(0) ; // getFinalRecord() ;
        
        double population = getPopulation() ; 
        for (String siteName : siteNames)
        {
            // Count infected siteName
            prevalence = countValueIncidence(siteName,TRUE,finalPrevalenceRecord,0)[1];
            finalPrevalencesRecord.put(siteName,prevalence/population) ;
        }
        
        // Count Agents with any Site infected
        ArrayList<String> agentRecords = extractArrayList(finalPrevalenceRecord,AGENTID) ;
        prevalence = 0 ;
        for (String record : agentRecords)
            for (String siteName : siteNames)
                if (record.contains(siteName))
                {
                    prevalence++ ;
                    break ;
                }
        finalPrevalencesRecord.put("all",prevalence/population) ;
        
        return finalPrevalencesRecord ;
    }
 
    /**
     * 
     * @param siteNames
     * @return Records of final symptomatic prevalences for specified siteNames and in total.
     */
    public HashMap<Object,Number> prepareFinalSymptomaticRecord(String[] siteNames)
    {
        HashMap<Object,Number> finalSymptomaticRecords = new HashMap<Object,Number>() ;
        
        int symptomatic ;
        
        String finalSymptomaticRecord = getFinalRecord() ;
        
        double population = getPopulation() ; 
        for (String siteName : siteNames)
        {
            // Count infected siteName
            symptomatic = countValueIncidence(siteName,TRUE,finalSymptomaticRecord,0)[0];
            finalSymptomaticRecords.put(siteName,symptomatic/population) ;
        }
        
        // Count Agents with any Site symptomatic 
        ArrayList<String> agentRecords = extractArrayList(finalSymptomaticRecord,AGENTID) ;
        symptomatic = 0 ;
        for (String record : agentRecords)
            for (String siteName : siteNames)
                if (compareValue(siteName, TRUE, record))
                {
                    symptomatic++ ;
                    break ;
                }
        finalSymptomaticRecords.put("all",symptomatic/population) ;
        
        return finalSymptomaticRecords ;
    }
    
    /**
     * 
     * @param backYears
     * @param backMonths
     * @param backDays
     * @return (HashMap) Report number of tests maps to number of Agents taking 
     * that many tests in given time frame.
     */
    public HashMap<Object,Number> prepareNumberAgentTestingReport(int backYears, int backMonths, int backDays)
    {
        HashMap<Object,Number> numberAgentTestingReport = new HashMap<Object,Number>() ;
        
        // (HashMap) agentId maps to (ArrayList) of cycles in which Agent was tested.
        HashMap<Object,ArrayList<Object>> agentTestingReport 
                = prepareAgentTestingReport(backYears, backMonths, backDays) ;
        
        int population = getPopulation() ;
        int untested = population ;
        
        int nbTests ;
        for (ArrayList<Object> value : agentTestingReport.values())
        {
            nbTests = value.size() ;
            numberAgentTestingReport = incrementHashMap(nbTests,numberAgentTestingReport) ;
            untested-- ;
        }
        numberAgentTestingReport.put(0, untested) ;
        
        for (Object tests : numberAgentTestingReport.keySet())
            numberAgentTestingReport.put(tests,(numberAgentTestingReport.get(tests).doubleValue())/population) ;
        
        return numberAgentTestingReport ;
    }
        
    /**
     * 
     * @param backYears
     * @param backMonths
     * @param backDays
     * @return (HashMap) agentId maps to (ArrayList) of cycles in which Agent was tested.
     */
    public HashMap<Object,ArrayList<Object>> prepareAgentTestingReport(int backYears, int backMonths, int backDays)
    {
        HashMap<Object,ArrayList<Object>> agentTestingReport = new HashMap<Object,ArrayList<Object>>() ; 
        
        int maxCycles = getMaxCycles() ;
        int backCycles = getBackCycles(backYears, backMonths, backDays, maxCycles) ;
        int startCycle = maxCycles - backCycles ;
        
        ArrayList<String> inputReport = getBackCyclesReport(backYears, backMonths, backDays) ;
        String record ;
        String agentId ;
        for (int cycle = 0 ; cycle < backCycles ; cycle++ )
        {
            record = inputReport.get(cycle) ;
            
            ArrayList<String> agentReport = extractArrayList(record,AGENTID,TESTED) ;
            for (String agentRecord : agentReport)
            {
                agentId = extractValue(AGENTID,agentRecord) ;
                updateHashMap(agentId, startCycle + cycle, agentTestingReport) ;
            }
        }
        return agentTestingReport ;
    }

    /**
     * FIXME: Correct order of category variables for ten or more treatments.
     * @param backYears
     * @param backMonths
     * @param backDays
     * @param sortingProperty
     * @return (HashMap) Report number of treatments maps to number of Agents 
     * receiving that many treatments in given time frame for each value of sortingProperty.
     */
    public HashMap<Object,Number[]> 
        prepareNumberAgentTreatedReport(int backYears, int backMonths, int backDays, String sortingProperty, int maxNumber)
    {
        HashMap<Object,Number[]> sortedNumberAgentTreatedReport 
                = new HashMap<Object,Number[]>() ;
        
        // (HashMap) agentId maps to (ArrayList) of cycles in which Agent was tested.
        HashMap<Object,ArrayList<Object>> agentTreatedReport 
                = prepareAgentTreatedReport(backYears, backMonths, backDays) ;
        
        // For sorting agentTreatedReport
        HashMap<Object,HashMap<Object,ArrayList<Object>>> sortedAgentTreatedReport 
                = new HashMap<Object,HashMap<Object,ArrayList<Object>>>() ;
        
        // Generate required spaces in label names
        //categoryEntry.sort(null);
        int totalDigits = (int) Math.log10(maxNumber) ; // (Integer.valueOf(String.valueOf(categoryEntry.get(categoryEntry.size() - 1))))) + 1 ;
        
        
        // Sort agentTreatedReport according to sortingProperty of Agents
        PopulationReporter populationReporter = new PopulationReporter(simName,getFolderPath()) ;
        int startCycle = getMaxCycles() - getBackCycles(backYears,backMonths, backDays) ;
        ArrayList<Object> agentsDeadRecord = populationReporter.prepareAgentsDeadRecord(startCycle) ;
        
        // agentId maps to sortingProperty
        HashMap<Object,Object> sortedAgentReport = populationReporter.sortedAgentIds(sortingProperty) ;
        //for (Object agentId : agentTreatedReport.keySet())
        for (Object agentId : sortedAgentReport.keySet())
        {
            if (agentsDeadRecord.contains(agentId))
                continue ;
            Object propertyKey = sortedAgentReport.get(agentId) ;
            // Create key if still needed
            if (!sortedAgentTreatedReport.containsKey(propertyKey))
                sortedAgentTreatedReport.put(propertyKey, new HashMap<Object,ArrayList<Object>>()) ;
            
            if (agentTreatedReport.containsKey(agentId))
                sortedAgentTreatedReport.get(propertyKey).put(agentId, agentTreatedReport.get(agentId)) ;
            else
                sortedAgentTreatedReport.get(propertyKey).put(agentId, new ArrayList<Object>()) ;
        }
        int nbKeys = sortedAgentTreatedReport.size() ;
        int sortedPopulation ;
        int nbTreatments ;
        int keyIndex = 0 ;
        String treatmentsString = "" ;
        String maxNumberString = String.valueOf(maxNumber).concat("+") ;
        for (Object propertyKey : sortedAgentTreatedReport.keySet())
        {
            HashMap<Object,Number> numberAgentTreatedReport = new HashMap<Object,Number>() ;
            
            HashMap<Object,ArrayList<Object>> sortedTreatedReport 
                    = sortedAgentTreatedReport.get(propertyKey) ;
            
            // Read in values corresponding to propertyKey
            sortedPopulation = sortedTreatedReport.size() ;
            for (ArrayList<Object> value : sortedTreatedReport.values())
            {
                nbTreatments = value.size() ;
                numberAgentTreatedReport = incrementHashMap(nbTreatments,numberAgentTreatedReport) ;
            }

            for (Object treatments : numberAgentTreatedReport.keySet())
                numberAgentTreatedReport.put(treatments,(numberAgentTreatedReport.get(treatments).doubleValue())/sortedPopulation) ;
        
            // Construct sortedNumberAgentTreatedReport
            for (Object treatments : numberAgentTreatedReport.keySet())
            {
                if ((Integer.valueOf(String.valueOf(treatments)) < maxNumber) || (maxNumber < 0))
                {
                    treatmentsString = String.valueOf(treatments) ;
                    int nbDigits = treatmentsString.length() ;
                    for (int digitIndex = nbDigits ; digitIndex < totalDigits ; digitIndex++ )
                        treatmentsString = " ".concat(treatmentsString) ;
                }
                else
                    treatmentsString = maxNumberString ;
                
                
                // Create keys where needed
                if (!sortedNumberAgentTreatedReport.containsKey(treatmentsString))
                {
                    sortedNumberAgentTreatedReport.put(treatmentsString, new Number[nbKeys]) ;
                    for (int index = 0 ; index < nbKeys ; index++ )
                        sortedNumberAgentTreatedReport.get(treatmentsString)[index] = 0 ;
                }
                
                // Enter values. maxNumber < 0 corresponds to no maximum.
                if (treatmentsString != maxNumberString) // (Integer.valueOf(String.valueOf(treatments)) < maxNumber) || maxNumber < 0)
                {
                    sortedNumberAgentTreatedReport.get(treatmentsString)[keyIndex] 
                        = numberAgentTreatedReport.get(treatments) ;
                }
                else
                {
                    sortedNumberAgentTreatedReport.get(maxNumberString)[keyIndex] 
                            = sortedNumberAgentTreatedReport.get(maxNumberString)[keyIndex].doubleValue()
                            + numberAgentTreatedReport.get(treatments).doubleValue() ;
                }
            }
            keyIndex++ ;
        }
        return sortedNumberAgentTreatedReport ;
    }
    
    /**
     * 
     * @param backYears
     * @param backMonths
     * @param backDays
     * @return (HashMap) Report number of treatments maps to number of Agents 
     * receiving that many treatments in given time frame.
     */
    public HashMap<Object,Number> prepareNumberAgentTreatedReport(int backYears, int backMonths, int backDays)
    {
        HashMap<Object,Number> numberAgentTreatedReport = new HashMap<Object,Number>() ;
        
        // (HashMap) agentId maps to (ArrayList) of cycles in which Agent was tested.
        HashMap<Object,ArrayList<Object>> agentTreatedReport 
                = prepareAgentTreatedReport(backYears, backMonths, backDays) ;
        
        int population = getPopulation() ;
        int untreated = population ;
        
        int nbTreatments ;
        for (ArrayList<Object> value : agentTreatedReport.values())
        {
            nbTreatments = value.size() ;
            numberAgentTreatedReport = incrementHashMap(nbTreatments,numberAgentTreatedReport) ;
            untreated-- ;
        }
        numberAgentTreatedReport.put(0, untreated) ;
        
        for (Object treatments : numberAgentTreatedReport.keySet())
            numberAgentTreatedReport.put(Integer.valueOf(String.valueOf(treatments)),(numberAgentTreatedReport.get(treatments).doubleValue())/population) ;
        
        return numberAgentTreatedReport ;
    }
 
    /**
     * 
     * @param backYears
     * @param backMonths
     * @param backDays
     * @return (HashMap) agentId maps to (ArrayList) of cycles in which Agent was treated for STIs.
     */
    public HashMap<Object,ArrayList<Object>> prepareAgentTreatedReport(int backYears, int backMonths, int backDays)
    {
        HashMap<Object,ArrayList<Object>> agentTreatedReport = new HashMap<Object,ArrayList<Object>>() ; 
        
        int maxCycles = getMaxCycles() ;
        int backCycles = getBackCycles(backYears, backMonths, backDays, maxCycles) ;
        int startCycle = maxCycles - backCycles ;
        
        ArrayList<String> inputReport = getBackCyclesReport(backYears, backMonths, backDays) ;
        String record ;
        String agentId ;
        for (int cycle = 0 ; cycle < backCycles ; cycle++ )
        {
            record = inputReport.get(cycle) ;
            
            ArrayList<String> agentReport = extractArrayList(record,AGENTID,TREATED) ;
            for (String agentRecord : agentReport)
            {
                agentId = extractValue(AGENTID,agentRecord) ;
                updateHashMap(agentId, startCycle + cycle, agentTreatedReport) ;
            }
        }
        return agentTreatedReport ;
    }
 
    /**
     * 
     * @return (ArrayList) indicating the total coprevalence, coprevalence of 
     * symptomatic infection, and proportion of symptomatic infection in each report cycle.
     */
    public ArrayList<Object> preparePrevalenceReport()
    {
        ArrayList<Object> prevalenceReport = new ArrayList<Object>() ;
        
        int population = getPopulation() ; // Integer.valueOf(getMetaDatum("Community.POPULATION")) ;
        int nbInfected ;
        int nbSymptomatic ;
        String entry ;
        String[] siteNames = MSM.SITE_NAMES ;
        boolean nextInput = true ; 

        while (nextInput)
        {
        
            for (int siteIndex = 0 ; siteIndex < siteNames.length ; siteIndex++ )
            {
                String siteName = siteNames[siteIndex] ;
                siteNames[siteIndex] = siteName.substring(0,1).toUpperCase() 
                        + siteName.substring(1) ;
            }
            for (String record : input)
            {
                nbSymptomatic = 0 ;
                ArrayList<String> infections = extractArrayList(record,AGENTID) ;
                nbInfected = infections.size() ;
                for (String infection : infections)
                    for (String siteName : MSM.SITE_NAMES)
                        if (compareValue(siteName,TRUE,infection))
                        {
                            nbSymptomatic++ ;
                            break ;
                        }

                //LOGGER.info(record) ;
                entry = addReportProperty("prevalence",((double) nbInfected)/population) ;
                entry += addReportProperty("symptomatic",((double) nbSymptomatic)/population) ;
                entry += addReportProperty("proportion",((double) nbSymptomatic)/nbInfected) ;

                prevalenceReport.add(entry) ;
            }
            nextInput = updateReport() ;
        }
        return prevalenceReport ;
    }
    
    /**
     * 
     * @param siteName
     * @return (ArrayList) indicating the total prevalence, prevalence of 
     * symptomatic infection, and proportion of symptomatic infection at site given 
     * by siteName in each report cycle.
     */
    public ArrayList<Object> preparePrevalenceReport(String siteName) 
    {
        ArrayList<Object> sitePrevalenceReport = new ArrayList<Object>() ;
        
        int population = Integer.valueOf(getMetaDatum("Community.POPULATION")) ;
        for (boolean nextInput = true ; nextInput ; nextInput = updateReport() )
        {
            int[] nbSymptomatic ;
            String entry ;
            for (String record : input)
            {
                nbSymptomatic = countValueIncidence(siteName,TRUE,record,0) ;

    //            if (nbSymptomatic[0] == nbSymptomatic[1])
    //                LOGGER.info(record);


                entry = addReportProperty("prevalence",((double) nbSymptomatic[1])/population) ;
                entry += addReportProperty("symptomatic",((double) nbSymptomatic[0])/population) ;
                entry += addReportProperty("proportion",((double) nbSymptomatic[0])/nbSymptomatic[1]) ;
                sitePrevalenceReport.add(entry) ;
            }
            
        }
        return sitePrevalenceReport ;
    }
    
    /**
     * 
     * @param siteNames
     * @return ArrayList of coprevalence of coninfection of Sites named in siteNames.
     */
    public ArrayList<Object> prepareCoPrevalenceReport(String[] siteNames) 
    {
        ArrayList<Object> siteCoPrevalenceReport = new ArrayList<Object>() ;
        
        int population = getPopulation() ; // Integer.valueOf(getMetaDatum("Community.POPULATION")) ;
        
        Double coprevalence ;
        String entry ;
        for (boolean nextInput = true ; nextInput ; nextInput = updateReport())
        {
            for (String record : input)
            {
                entry = record ;
                for (String siteName : siteNames)
                {
                    entry = boundedStringByContents(siteName,AGENTID,entry) ;
                }
                if (entry.isEmpty())
                    coprevalence = 0.0 ;
                else 
                    coprevalence = (Double.valueOf(countValueIncidence(AGENTID,"",entry,0)[1]))/population ;
                siteCoPrevalenceReport.add("coprevalence:" + String.valueOf(coprevalence) + " ") ;
                //LOGGER.info("coprevalence:" + String.valueOf(coprevalence) + " ");
            }
        }
        return siteCoPrevalenceReport ;
    }
    
    /** 
     * 
     * @return Report indicating the number and per population of incidents
     * of infection.
     */
    public ArrayList<Object> prepareNotificationsReport()
    {
        return ScreeningReporter.this.prepareNotificationsReport("") ;
    }
    
    public ArrayList<Object> prepareNotificationsReport(String siteName)
    {
        ArrayList<Object> notificationsReport = new ArrayList<Object>() ;
        
        int notifications ;
        double rate ;
        int population = getPopulation() ;
        String output ;
        
        // Loop through Reporter input files 
        for (boolean nextInput = true ; nextInput ; nextInput = updateReport() )
            for (String record : input)
            {
                if (!siteName.isEmpty())
                    record = boundedStringByContents(siteName,AGENTID,record) ;
                
                notifications = countValueIncidence("treated","",record,0)[1];
                rate = ((double) notifications)/population;

                output = Reporter.addReportProperty("notification", notifications) ;
                output += Reporter.addReportProperty("rate", rate) ;

                notificationsReport.add(output) ;
            }
        return notificationsReport ;
    }
    
}
