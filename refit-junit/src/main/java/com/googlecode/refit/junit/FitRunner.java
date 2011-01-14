/*
 * Copyright 2011 Harald Wellmann
 *
 * This file is part of reFit.
 * 
 * reFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * reFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with reFit.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.googlecode.refit.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import fit.Fixture;
import fit.Parse;

/**
 * Runs a single FIT test under JUnit. Do not directly use this class in applications.
 * 
 * @see FitSuite
 * @author hwellmann
 *
 */
public class FitRunner extends Runner {
   
    /** Input directory for FIT tests. */
    private File inputDir;
    
    /** Output directory for FIT tests. */
    private File outputDir;
    
    /** 
     * Relative path of fit test to be run. The runner runs a file with this path relative to
     * the input directory and write the processed to file to the same path relative to the
     * output directory.
     */
    private String testPath;
    
    /**
     * Description of this FIT test for JUnit notifiers. Corresponds to {@code testPath}.
     */
    private Description description;
    
    /** Default fixture for running the test. */
    private Fixture fixture;
    
    public FitRunner(File inputDir, File outputDir, String testPath) {
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.testPath = testPath;
        this.description = Description.createSuiteDescription(testPath);
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public void run(RunNotifier notifier) {
        File inputFile = new File(inputDir, testPath);
        File outputFile = new File(outputDir, testPath);
        System.setProperty("fit.currentTest", testPath);
        notifier.fireTestStarted(getDescription());
        try {
            ensureParentDirExists(outputFile);
            run(inputFile, outputFile);
            if (failed(fixture)) {
                /*
                 * Distinguish failed tests from tests in error by including the appropriate
                 * type of Throwable in the Failure.
                 */
                Failure failure = new Failure(getDescription(), getThrowable());
                notifier.fireTestFailure(failure);
            }
        }
        catch (IOException exc) {
            Failure failure = new Failure(getDescription(), exc);
            notifier.fireTestFailure(failure);
        }
        catch (ParseException exc) {
            Failure failure = new Failure(getDescription(), exc);
            notifier.fireTestFailure(failure);
        }
        notifier.fireTestFinished(getDescription());
    }

    /**
     * Returns an AssertionError for failed tests and an Exception for tests with unexpected
     * exceptions, to match JUnit conventions.
     * @return Throwable
     */
    private Throwable getThrowable() {
        if (fixture.counts.exceptions > 0)
            return new Exception(fixture.counts());
        else
            return new AssertionError(fixture.counts());
    }

    private void ensureParentDirExists(File outputFile) throws IOException {
        File parentDir = outputFile.getParentFile();
        if (parentDir.exists()) {
            if (!parentDir.isDirectory()) {
                throw new IOException(parentDir + " is not a directory");
            }
        }
        else if (!parentDir.mkdirs()) {
            throw new IOException("cannot create " + parentDir);
        }
    }
    
    private void run(File in, File out) throws IOException, ParseException {
        run(new FileReader(in), new FileWriter(out));
    }

    private void run(Reader reader, Writer writer) throws IOException, ParseException {
        String input = read(reader);
        Parse tables = new Parse(input);
        fixture = new Fixture();
        fixture.doTables(tables);
        PrintWriter output = new PrintWriter(writer);
        tables.print(output);
        output.flush();
    }

    private boolean failed(Fixture fixture) {
        if (fixture.counts.wrong > 0 || fixture.counts.exceptions > 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns the whole stream as a String.
     * @param in
     * @return
     * @throws IOException
     */
    private String read(Reader in) throws IOException {
        BufferedReader br = new BufferedReader(in);
        StringBuffer sb = new StringBuffer();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }
        in.close();
        return sb.toString();
    }
}
