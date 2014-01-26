package org.solenopsis.tooling.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.LogManager;
import org.flossware.util.properties.impl.FilePropertiesMgr;
import org.solenopsis.lasius.credentials.Credentials;
import org.solenopsis.lasius.credentials.impl.PropertiesCredentials;
import org.solenopsis.lasius.wsimport.session.mgr.impl.SingleSessionMgr;
import org.solenopsis.wsdls.tooling.ApexClass;
import org.solenopsis.wsdls.tooling.ApexCodeCoverageAggregate;
import org.solenopsis.wsdls.tooling.ApexTrigger;
import org.solenopsis.wsdls.tooling.Error;
import org.solenopsis.wsdls.tooling.QueryResult;
import org.solenopsis.wsdls.tooling.SObject;
import org.solenopsis.wsdls.tooling.SforceServicePortType;
import org.solenopsis.wsdls.tooling.SforceServiceService;
import org.solenopsis.lasius.wsimport.util.SalesforceWebServiceUtil;
import org.solenopsis.wsdls.tooling.ApexClassMember;
import org.solenopsis.wsdls.tooling.ContainerAsyncRequest;
import org.solenopsis.wsdls.tooling.DeleteResult;
import org.solenopsis.wsdls.tooling.DescribeGlobalResult;
import org.solenopsis.wsdls.tooling.DescribeGlobalSObjectResult;
import org.solenopsis.wsdls.tooling.Field;
import org.solenopsis.wsdls.tooling.MetadataContainer;
import org.solenopsis.wsdls.tooling.SaveResult;


/**
 *
 * The purpose of this class is emit tooling to the console.
 *
 * @author sfloess
 *
 */
public class Main {
//
//    private static Map<String, ApexClass> getApexClasses(final SforceServicePortType port) throws Exception {
//        final Map<String, ApexClass> retVal = new HashMap<>();
//
//        boolean isDone = false;
//        QueryResult queryResult = port.query(SOQL_CLASSES);
//
//        while (!isDone) {
//            for(final SObject sobj : queryResult.getRecords()) {
//                retVal.put(sobj.getId(), (ApexClass) sobj);
//            }
//
//            if (queryResult.isDone()) {
//                isDone = true;
//            } else {
//                queryResult = port.queryMore(queryResult.getQueryLocator());
//            }
//        }
//
//        return retVal;
//    }
//
//    private static Map<String, ApexTrigger> getApexTriggers(final SforceServicePortType port) throws Exception {
//        final Map<String, ApexTrigger> retVal = new HashMap<>();
//
//        QueryResult queryResult = port.query(SOQL_TRIGGERS);
//        boolean isDone = false;
//
//        while (!isDone) {
//            for(final SObject sobj : queryResult.getRecords()) {
//                retVal.put(sobj.getId(), (ApexTrigger) sobj);
//            }
//
//            if (queryResult.isDone()) {
//                isDone = true;
//            } else {
//                queryResult = port.queryMore(queryResult.getQueryLocator());
//            }
//        }
//
//        return retVal;
//    }
//
//    private static Map<String, ApexCodeCoverageAggregate> getApexCodeCoverage(final SforceServicePortType port) throws Exception {
//        final Map<String, ApexCodeCoverageAggregate> retVal = new HashMap<>();
//
//        QueryResult queryResult = port.query(SOQL_COVERAGE);
//        boolean isDone = false;
//
//        while (!isDone) {
//            for(final SObject sobj : queryResult.getRecords()) {
//                retVal.put(sobj.getId(), (ApexCodeCoverageAggregate) sobj);
//            }
//
//            if (queryResult.isDone()) {
//                isDone = true;
//            } else {
//                queryResult = port.queryMore(queryResult.getQueryLocator());
//            }
//        }
//
//        return retVal;
//    }

//    private static double computePercentage(final ApexCodeCoverageAggregate coverage) {
//        if (coverage.getCoverage().getUncoveredLines().isEmpty()) {
//            return 0;
//        }
//
//        return ((double) coverage.getCoverage().getCoveredLines().size() / (coverage.getCoverage().getCoveredLines().size() + coverage.getCoverage().getUncoveredLines().size())) * 100;
//    }

    private static final String SOQL_COVERAGE = "select ApexClassorTriggerId, Id, NumLinesCovered, NumLinesUncovered, Coverage from ApexCodeCoverageAggregate ";
    private static final String SOQL_CLASSES = "select Id, Name from ApexClass";
    private static final String SOQL_TRIGGERS = "select Id, Name from ApexTrigger";

    private static <S> Map<String, S> getData(final SforceServicePortType port, final String soql) throws Exception {
        final Map<String, S> retVal = new HashMap<>();

        QueryResult queryResult = port.query(soql);
        boolean isDone = false;

        while (!isDone) {
            for(final SObject sobj : queryResult.getRecords()) {
                retVal.put(sobj.getId(), (S) sobj);
            }

            if (queryResult.isDone()) {
                isDone = true;
            } else {
                queryResult = port.queryMore(queryResult.getQueryLocator());
            }
        }

        return retVal;
    }

    private static double computePercentage(final ApexCodeCoverageAggregate codeCoverage) {
        if (codeCoverage.getCoverage().getUncoveredLines().isEmpty()) {
            return 0;
        }

//        return ((double) coverage.getNumLinesCovered() / (coverage.getNumLinesCovered() + coverage.getNumLinesUncovered())) * 100;
        return ((double) codeCoverage.getCoverage().getCoveredLines().size() / (codeCoverage.getCoverage().getCoveredLines().size() + codeCoverage.getCoverage().getUncoveredLines().size())) * 100;
    }

    private static String computeLine(final String name, final ApexCodeCoverageAggregate codeCoverage) {
        if (null == codeCoverage) {
            return String.format("%-40s%13d%13d%13.2f", name, 0, 0, 0.0);
        }

        return String.format("%-40s%13d%13d%13.2f", name, codeCoverage.getCoverage().getCoveredLines().size(), codeCoverage.getCoverage().getUncoveredLines().size(), computePercentage(codeCoverage));
    }

    private static Map<String, ApexCodeCoverageAggregate> getCodeCoverage(final SforceServicePortType port) throws Exception {
        final Map<String, ApexCodeCoverageAggregate> coverageMap = getData(port, SOQL_COVERAGE);
        final Map<String, ApexCodeCoverageAggregate> retVal = new HashMap<>();

        for (ApexCodeCoverageAggregate coverage : coverageMap.values()) {
            retVal.put(coverage.getApexClassOrTriggerId(), coverage);
        }

        return retVal;
    }

    private static Map<String, String> getClassCoverage(final SforceServicePortType port, final Map<String, ApexCodeCoverageAggregate> coverageMap) throws Exception {
        final Map<String, ApexClass> map    = getData(port, SOQL_CLASSES);
        final Map<String, String>    retVal = new TreeMap<>();

        for (final ApexClass apexClass : map.values()) {
            retVal.put(apexClass.getName(), computeLine(apexClass.getName(), coverageMap.get(apexClass.getId())));
        }



        return retVal;
    }

    private static Map<String, String> getTriggerCoverage(final SforceServicePortType port, final Map<String, ApexCodeCoverageAggregate> coverageMap) throws Exception {
        final Map<String, ApexTrigger> map    = getData(port, SOQL_TRIGGERS);
        final Map<String, String>      retVal = new TreeMap<>();

        for (final ApexTrigger apexTrigger : map.values()) {
            retVal.put(apexTrigger.getName(), computeLine(apexTrigger.getName(), coverageMap.get(apexTrigger.getId())));
        }

        return retVal;
    }

    private static void emitTooling(final String wsdlType, Credentials creds) throws Exception {
        final SforceServicePortType port = SalesforceWebServiceUtil.createToolingProxyPort(new SingleSessionMgr(creds), SforceServiceService.class);

        final Map<String, ApexCodeCoverageAggregate> coverageMap = getCodeCoverage(port);

        final Map<String, String> classCoverage = getClassCoverage(port, coverageMap);

        System.out.println("\n\n");
        System.out.println("Class Coverage:");
        System.out.printf("    %40s%13s%13s%15s", " ", "Covered", "Uncovered", "Percent\n");

        for (final String str : classCoverage.values()) {
            System.out.println("    " + str + "%");
        }

        final Map<String, String> triggerCoverage = getTriggerCoverage(port, coverageMap);

        System.out.println("\n\n");
        System.out.println("Trigger Coverage:");
        System.out.printf("    %40s%13s%13s%15s", " ", "Covered", "Uncovered", "Percent\n");

        for (final String str : triggerCoverage.values()) {
            System.out.println("    " + str + "%");
        }
    }

    public static void processErrors(final String prefix, final List<Error> errorList) {
        for (final Error error : errorList) {
            System.out.println(prefix + error.getMessage() + " [" + error.getStatusCode().name() + " - " + error.getStatusCode().value() + "] (" + error.getFields().size() + "):");
            for (final String field : error.getFields()) {
                System.out.println(prefix + "    " + field);
            }
        }
    }

    public static void processErrors(final List<Error> errorList) {
        processErrors("    ", errorList);
    }

    public static void processSaveResults(final List<SaveResult> srList) {
        for (final SaveResult sr : srList) {
            System.out.println(sr.getId() + " -> " + sr.isSuccess() + " (" + sr.getErrors().size() + "):");

            if (!sr.isSuccess()) {
                processErrors(sr.getErrors());
            }
        }
    }

    public static void emitMetadataTypes(final String wsdlType, Credentials creds) throws Exception {
        final SforceServicePortType port = SalesforceWebServiceUtil.createToolingProxyPort(new SingleSessionMgr(creds), SforceServiceService.class);

        final DescribeGlobalResult dgr = port.describeGlobal();

        System.out.printf("\n\n%-45s%-45s\n", "NAME", "LABEL");

        for (final DescribeGlobalSObjectResult dgsor : dgr.getSobjects()) {
            System.out.printf("%-45s%-45s\n", dgsor.getName(), dgsor.getLabel());
        }
    }

    public static void runTest(final String wsdlType, Credentials creds) throws Exception {
        final SforceServicePortType port = SalesforceWebServiceUtil.createToolingProxyPort(new SingleSessionMgr(creds), SforceServiceService.class);

        final MetadataContainer container = new MetadataContainer();
        container.setName("TestResults"+System.currentTimeMillis());

        final List<SObject> containerList = new ArrayList<SObject>();
        containerList.add(container);

        final List<SaveResult> containerSaveResultList = port.create(containerList);

        final String containerId = containerSaveResultList.get(0).getId();

        System.out.println("Container id [" + containerId + "]");

        final ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setMetadataContainerId(containerId);
        car.setIsRunTests(true);

        final List<SObject> carList = new ArrayList<SObject>();
        carList.add(car);

        processSaveResults(port.create(carList));
    }

    public static void deleteContainers(final String wsdlType, Credentials creds) throws Exception {
        final SforceServicePortType port = SalesforceWebServiceUtil.createToolingProxyPort(new SingleSessionMgr(creds), SforceServiceService.class);
//
//        final MetadataContainer container = new MetadataContainer();
//        container.setName("TestResults");
//
//        final List<SObject> containerList = new ArrayList<SObject>();
//        containerList.add(container);

//        final DescribeGlobalResult dgr = port.describeGlobal();
//
//        for (final DescribeGlobalSObjectResult dgsor : dgr.getSobjects()) {
//            System.out.println(dgsor.getLabel() + "  " + dgsor.getName());
//        }

        final QueryResult qr = port.query("select Id, Name from MetadataContainer");
        final List<String> toDelete = new ArrayList<String>(qr.getSize());
        for (final SObject sobj : qr.getRecords()) {
            final MetadataContainer mc = (MetadataContainer) sobj;
            System.out.println(mc.getId() + " " + mc.getName());
            toDelete.add(mc.getId());
        }

        final List<DeleteResult> drList = port.delete(toDelete);

        for (final DeleteResult dr : drList) {
            System.out.println(dr.getId() + " " + dr.isSuccess());
            System.out.println(dr.getErrors());
        }
    }

    public static void main(final String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
//
        final String env = "sfloess.properties";
//        final String env = "test-dev.properties";
////        final String env = "qa.properties";
////        final String env = "dev.properties";
//
//        long start1 = System.currentTimeMillis();
//
//        emitTooling("Partner WSDL", new PropertiesCredentials(new FilePropertiesMgr(System.getProperty("user.home") + "/.solenopsis/credentials/" + env)));
//
//        long stop1 = System.currentTimeMillis();
//
//        long start2 = System.currentTimeMillis();
//
//        emitTooling("Partner WSDL", new PropertiesCredentials(new FilePropertiesMgr(System.getProperty("user.home") + "/.solenopsis/credentials/" + env)));
//
//        long stop2 = System.currentTimeMillis();
//
//        System.out.println((stop1 - start1) + " -> " + (stop2 - start2));

//        runTest("Partner WSDL", new PropertiesCredentials(new FilePropertiesMgr(System.getProperty("user.home") + "/.solenopsis/credentials/" + env)));
//        deleteContainers("Partner WSDL", new PropertiesCredentials(new FilePropertiesMgr(System.getProperty("user.home") + "/.solenopsis/credentials/" + env)));
        emitMetadataTypes("Partner WSDL", new PropertiesCredentials(new FilePropertiesMgr(System.getProperty("user.home") + "/.solenopsis/credentials/" + env)));
    }
}