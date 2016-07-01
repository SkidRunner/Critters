package karpathy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>Global</code> represents values and methods that are shared throughout the application.
 * <p>
 * This implementation of settings.h and helpers.h combine.
 *
 * @author Skidrunner
 */
public class Global {

    /**
     * Number of inputs each scriptsbot has.
     */
    public static final int INPUTSIZE = 25;
    /**
     * Number of outputs each scriptsbot has.
     */
    public static final int OUTPUTSIZE = 9;
    /**
     * Number of eyes each scriptsbot has.
     */
    public static final int NUMEYES = 4;
    /**
     * Number of neurons in each scriptsbot brain.
     */
    public static final int BRAINSIZE = 200;
    /**
     * Number of connections each neuron in a sciptbots brain has.
     */
    public static final int CONNS = 4;

    private static final Logger _LOGGER = Logger.getLogger(Global.class.getName());
    private static final Properties _PROPERTIES;
    private static boolean _DEVIATE_AVAILABLE = false;
    private static double _STORED_DEVIATE;

    static {
        Properties defaultProperties = new Properties();
        try {
            defaultProperties.loadFromXML(Global.class.getResourceAsStream("properties.xml"));
        } catch (IOException e) {
            _LOGGER.log(Level.SEVERE, "Failed to load properties form resource.", e);
            throw new RuntimeException("Failed to load properties form resource.", e);
        }
        _PROPERTIES = new Properties(defaultProperties);
    }

    /**
     * Searches for the property with the specified name in this property list.
     *
     * @param name the property name.
     * @return the value in this property list with the specified key value.
     */
    public static final String GetProperty(String name) {
        synchronized (_PROPERTIES) {
            return _PROPERTIES.getProperty(name);
        }
    }

    /**
     * Creates or replaces a value with the specified name in this property list.
     *
     * @param name  the property name.
     * @param value the value to set in this property list.
     */
    public static final void SetProperty(String name, String value) {
        synchronized (_PROPERTIES) {
            _PROPERTIES.setProperty(name, value);
        }
    }

    /**
     * Loads all of the properties represented by the document on the specified input stream into this properties table.
     *
     * @param inputStream the input stream from which to read the document.
     * @throws IOException if reading from the specified input stream results in an IOException.
     */
    public static final void LoadProperties(InputStream inputStream) throws IOException {
        try {
            try {
                _PROPERTIES.loadFromXML(inputStream);
            } catch (InvalidPropertiesFormatException e) {
                _PROPERTIES.load(inputStream);
            }
        } catch (IOException e) {
            _LOGGER.log(Level.WARNING, "Failed to load properties form file.", e);
            throw e;
        }
    }

    /**
     * Emits an XML document representing all of the properties contained in this table.
     *
     * @param outputStream the output stream on which to emit the XML document.
     * @throws IOException if writing to the specified output stream results in an IOException.
     */
    public static final void SaveProperties(OutputStream outputStream) throws IOException {
        try {
            _PROPERTIES.storeToXML(outputStream, new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").format(new Date()), "UTF-8");
        } catch (IOException e) {
            _LOGGER.log(Level.WARNING, "Failed to save properties to file.", e);
            throw e;
        }
    }

    public static final double RandomVariate(double multiplier, double summation) {
        if (!_DEVIATE_AVAILABLE) {
            double polar;
            double var1;
            double var2;
            double rsquared;

            do {
                var1 = 2.0 * Math.random() - 1.0;
                var2 = 2.0 * Math.random() - 1.0;
                rsquared = var1 * var1 + var2 * var2;
            } while (rsquared >= 1.0 || rsquared == 0.0);

            polar = Math.sqrt((-2.0 * Math.log(rsquared)) / rsquared);

            _STORED_DEVIATE = var1 * polar;
            _DEVIATE_AVAILABLE = true;

            return ((var2 * polar) * summation) + multiplier;
        }

        _DEVIATE_AVAILABLE = false;
        return (_STORED_DEVIATE * summation) + multiplier;
    }

    public static final int RandomInt() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }

    public static final int RandomInt(int min, int max) {
        return (int) Math.round((Math.random() * (max - min)) + min);
    }

    public static final float RandomFloat(float min, float max) {
        return (float) ((Math.random() * (max - min)) + min);
    }

    public static final double RandomDouble(double min, double max) {
        return (float) ((Math.random() * (max - min)) + min);
    }
}
