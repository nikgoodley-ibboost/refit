package com.googlecode.refit.runner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.googlecode.refit.runner.jaxb.ObjectFactory;
import com.googlecode.refit.runner.jaxb.Summary;
import com.googlecode.refit.runner.jaxb.TestResult;

public class ReportIO {

    public static final String FIT_REPORT_XML = "fit-report.xml";
    public static final String FIT_REPORT_HTML = "index.html";
    public static final String FIT_CSS = "fit.css";
    public static final String CONTEXT_PATH = "com.googlecode.refit.runner.jaxb";

    private Summary summary;
    private PrintWriter writer;

    public ReportIO(Summary summary) {
        this.summary = summary;
    }
    
    public void writeCss(File css) throws IOException {
        writer = new PrintWriter(css, "UTF-8");
        InputStream is = getClass().getResourceAsStream("/css/fit.css");
        InputStreamReader reader = new InputStreamReader(is);
        String content = FileRunner.read(reader);
        writer.write(content);
        writer.close();
    }
    
    public void writeXml(File report) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(CONTEXT_PATH);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        ObjectFactory factory = new ObjectFactory();
        marshaller.marshal(factory.createSummary(summary), report);        
    }
    
    public void writeHtml(File report) throws IOException {
        writer = new PrintWriter(report, "UTF-8");
        writer.println("<html><head><title>Fit Summary</title>");
        writer.println("<link rel='stylesheet' type='text/css' href='" + FIT_CSS + "'>");
        writer.println("</head><body>");
        writer.println("<table class='summary'>");
        writeTableHeaderRow();
        writeTotals();
        for (TestResult testResult : summary.getTest()) {
            writeTableRow(testResult);
        }
        writer.println("</table>");
        writer.println("</body></html>");
        writer.close();
    }

    private void writeTotals() {
        writer.println("<tr>");
        writeTableCell("total", "Total (" + summary.getNumTests() + " tests)");
        writeTableCell("right", summary.getRight());
        writeTableCell("wrong", summary.getWrong());
        writeTableCell("ignored", summary.getIgnored());
        writeTableCell("exceptions", summary.getExceptions());
        writer.println("</tr>");
    }


    
    private void writeTableHeaderRow() {
        writer.println("<tr>");
        writeTableHeaderCell("Test");
        writeTableHeaderCell("Right");
        writeTableHeaderCell("Wrong");
        writeTableHeaderCell("Ignored");
        writeTableHeaderCell("Exceptions");
        writer.println("</tr>");
    }

    private void writeTableHeaderCell(String text) {
        writer.println("<th>" + text + "</th>");
    }

    private void writeTableRow(TestResult testResult) {
        writer.println("<tr>");
        writeLinkedTableCell(testResult.getPath());
        writeTableCell("right", testResult.getRight());
        writeTableCell("wrong", testResult.getWrong());
        writeTableCell("ignored", testResult.getIgnored());
        writeTableCell("exceptions", testResult.getExceptions());
        writer.println("</tr>");
    }

    private void writeTableCell(String style, String text) {
        writer.println("<td class='"+ style +"'>" + text + "</td>");
    }

    private void writeLinkedTableCell(String text) {
        writer.println("<td><a href='" + text + "'>" + text + "</a></td>");
    }

    private void writeTableCell(String style, int count) {
        String actualStyle = (count == 0) ? "none" : style;
        writer.println("<td class='"+ actualStyle +"'>" + count + "</td>");
    }
}
