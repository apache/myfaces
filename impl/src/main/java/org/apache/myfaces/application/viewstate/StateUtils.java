/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.application.viewstate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jakarta.faces.FacesException;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExternalContext;
import jakarta.servlet.ServletContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.core.api.shared.lang.Assert;
import org.apache.myfaces.spi.SerialFactory;

/**
 * <p>This Class exposes a handful of methods related to encryption,
 * compression and serialization of the view state.</p>
 * 
 * <ul>
 * <li>ISO-8859-1 is the character set used.</li>
 * <li>GZIP is used for all compression/decompression.</li>
 * <li>Base64 is used for all encoding and decoding.</li>
 * <li>AES is the default encryption algorithm</li>
 * <li>ECB is the default mode</li>
 * <li>PKCS5Padding is the default padding</li>
 * <li>HmacSHA256 is the default MAC algorithm</li>
 * <li>The default algorithm can be overridden using the
 * <i>org.apache.myfaces.ALGORITHM</i> parameter</li>
 * <li>The default mode and padding can be overridden using the
 * <i>org.apache.myfaces.ALGORITHM.PARAMETERS</i> parameter</li>
 * <li>This class has not been tested with modes other than ECB and CBC</li>
 * <li>An initialization vector can be specified via the
 * <i>org.apache.myfaces.ALGORITHM.IV</i> parameter</li>
 * <li>The default MAC algorithm can be overridden using the
 * <i>org.apache.myfaces.MAC_ALGORITHM</i> parameter</li>
 * </ul>
 *
 * <p>The secret is interpreted as base 64 encoded.  In other
 * words, if your secret is "76543210", you would put "NzY1NDMyMTA=" in
 * the deployment descriptor.  This is needed so that key values are not
 * limited to just values composed of printable characters.</p>
 *
 * <p>If you are using CBC mode encryption, you <b>must</b> specify an
 * initialization vector.</p>
 *
 * <p>If you are using the AES algorithm and getting a SecurityException
 * complaining about keysize, you most likely need to get the unlimited
 * strength jurisdiction policy files from a place like
 * http://java.sun.com/j2se/1.4.2/download.html .</br>
 * Since https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8170157
 * unlimited cryptographic policy is enabled by default.</p>
 *
 *
 * See org.apache.myfaces.webapp.StartupServletContextListener
 */
public final class StateUtils
{
    private static final Logger log = Logger.getLogger(StateUtils.class.getName());

    public static final String ZIP_CHARSET = "ISO-8859-1";

    public static final String DEFAULT_ALGORITHM = "AES";
    public static final String DEFAULT_ALGORITHM_PARAMS = "ECB/PKCS5Padding";

    public static final String INIT_PREFIX = "org.apache.myfaces.";
    
    /**
     * Indicate if the view state is encrypted or not. By default, encryption is enabled.
     */
    @JSFWebConfigParam(name="org.apache.myfaces.USE_ENCRYPTION",since="1.1",
            defaultValue="true",expectedValues="true,false",group="state")
    public static final String USE_ENCRYPTION = INIT_PREFIX + "USE_ENCRYPTION";
    
    /**
     * Defines the secret (Base64 encoded) used to initialize the secret key
     * for encryption algorithm. See MyFaces wiki/web site documentation 
     * for instructions on how to configure an application for 
     * different encryption strengths.
     */
    @JSFWebConfigParam(name="org.apache.myfaces.SECRET",since="1.1",group="state")
    public static final String INIT_SECRET = INIT_PREFIX + "SECRET";
    
    /**
     * Indicate the encryption algorithm used for encrypt the view state.
     */
    @JSFWebConfigParam(name="org.apache.myfaces.ALGORITHM",since="1.1",
            defaultValue="AES",group="state",tags="performance")
    public static final String INIT_ALGORITHM = INIT_PREFIX + "ALGORITHM";

    /**
     * If is set to "false", the secret key used for encryption algorithm is not cached. This is used
     * when the returned SecretKey for encryption algorithm is not thread safe. 
     */
    @JSFWebConfigParam(name="org.apache.myfaces.SECRET.CACHE",since="1.1",group="state")
    public static final String INIT_SECRET_KEY_CACHE = INIT_SECRET + ".CACHE";
    
    /**
     * Defines the initialization vector (Base64 encoded) used for the encryption algorithm
     */
    @JSFWebConfigParam(name="org.apache.myfaces.ALGORITHM.IV",since="1.1",group="state")
    public static final String INIT_ALGORITHM_IV = INIT_ALGORITHM + ".IV";
    
    /**
     * Defines the default mode and padding used for the encryption algorithm
     */
    @JSFWebConfigParam(name="org.apache.myfaces.ALGORITHM.PARAMETERS",since="1.1",
            defaultValue="ECB/PKCS5Padding",group="state")
    public static final String INIT_ALGORITHM_PARAM = INIT_ALGORITHM + ".PARAMETERS";
    
    /**
     * Defines the factory class name using for serialize/deserialize the view state returned 
     * by state manager into a byte array. The expected class must implement
     * {@link org.apache.myfaces.spi.SerialFactory} interface.
     */
    @JSFWebConfigParam(name="org.apache.myfaces.SERIAL_FACTORY", since="1.1",group="state",tags="performance")
    public static final String SERIAL_FACTORY = INIT_PREFIX + "SERIAL_FACTORY";
    
    /**
     * Indicate if the view state should be compressed before encrypted(optional) and encoded
     */
    @JSFWebConfigParam(name="org.apache.myfaces.COMPRESS_STATE_IN_CLIENT",since="1.1",defaultValue="false",
            expectedValues="true,false",group="state",tags="performance")
    public static final String COMPRESS_STATE_IN_CLIENT = INIT_PREFIX + "COMPRESS_STATE_IN_CLIENT";

    public static final String DEFAULT_MAC_ALGORITHM = "HmacSHA256";

    /**
     * Indicate the algorithm used to calculate the Message Authentication Code that is
     * added to the view state.
     */
    @JSFWebConfigParam(name="org.apache.myfaces.MAC_ALGORITHM",defaultValue="HmacSHA256",
            group="state",tags="performance")
    public static final String INIT_MAC_ALGORITHM = "org.apache.myfaces.MAC_ALGORITHM";
    
    /**
     * Define the initialization code that are used to initialize the secret key used
     * on the Message Authentication Code algorithm
     */
    @JSFWebConfigParam(name="org.apache.myfaces.MAC_SECRET",group="state")
    public static final String INIT_MAC_SECRET = "org.apache.myfaces.MAC_SECRET";

    /**
     * If is set to "false", the secret key used for MAC algorithm is not cached. This is used
     * when the returned SecretKey for mac algorithm is not thread safe. 
     */
    @JSFWebConfigParam(name="org.apache.myfaces.MAC_SECRET.CACHE",group="state")
    public static final String INIT_MAC_SECRET_KEY_CACHE = "org.apache.myfaces.MAC_SECRET.CACHE";
    
    /** Utility class, do not instatiate */
    private StateUtils()
    {
        //nope
    }

    private static void testConfiguration(ExternalContext ctx)
    {
        String algorithmParams = ctx.getInitParameter(INIT_ALGORITHM_PARAM);
        if (algorithmParams != null && algorithmParams.startsWith("CBC"))
        {
            String iv = ctx.getInitParameter(INIT_ALGORITHM_IV);
            if (iv == null)
            {
                throw new FacesException(INIT_ALGORITHM_PARAM +
                        " parameter has been set with CBC mode," +
                        " but no initialization vector has been set " +
                        " with " + INIT_ALGORITHM_IV);
            }
        }
    }
    
    public static Cipher createCipher(ExternalContext externalContext, int mode) throws Exception
    {
        SecretKey secretKey = (SecretKey) getSecret(externalContext);
        String algorithm = findAlgorithm(externalContext);
        String algorithmParams = findAlgorithmParams(externalContext);
        byte[] iv = findInitializationVector(externalContext);

        Cipher cipher = Cipher.getInstance(algorithm + '/' + algorithmParams);
        if (iv != null)
        {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(mode, secretKey, ivSpec);
        }
        else
        {
            cipher.init(mode, secretKey);
        }

        if (log.isLoggable(Level.FINE))
        {
            log.fine("de/encrypting with " + algorithm + '/' + algorithmParams);
        }

        return cipher;
    }
    
    public static Mac createMac(ExternalContext externalContext) throws Exception
    {
        SecretKey macSecretKey = (SecretKey) getMacSecret(externalContext);
        String macAlgorithm = findMacAlgorithm(externalContext);
        
        Mac mac = Mac.getInstance(macAlgorithm);
        mac.init(macSecretKey);

        return mac;
    }
    
    public static boolean enableCompression(ExternalContext externalContext)
    {
        Assert.notNull(externalContext, "externalContext");

        return "true".equals(externalContext.getInitParameter(COMPRESS_STATE_IN_CLIENT));
    }
    
    public static boolean isSecure(ExternalContext externalContext)
    {
        Assert.notNull(externalContext, "externalContext");
        
        return !"false".equals(externalContext.getInitParameter(USE_ENCRYPTION));
    }

    /**
     * This fires during the Render Response phase, saving state.
     */
    public static final String construct(Object object, ExternalContext ctx)
    {
        byte[] bytes = getAsByteArray(object, ctx);
        if (enableCompression(ctx))
        {
            bytes = compress(bytes);
        }
        if (isSecure(ctx))
        {
            bytes = encrypt(bytes, ctx);
        }
        bytes = encode(bytes);

        try
        {
            return new String(bytes, ZIP_CHARSET);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new FacesException(e);
        }
    }

    /**
     * Performs serialization with the serialization provider created by the 
     * SerialFactory.  
     * 
     * @param object
     * @param ctx
     * @return
     */
    public static final byte[] getAsByteArray(Object object, ExternalContext ctx)
    {
        // get the Factory that was instantiated @ startup
        SerialFactory serialFactory = (SerialFactory) ctx.getApplicationMap().get(SERIAL_FACTORY);
        Assert.notNull(serialFactory, "serialFactory");

        try
        {
            return serialFactory.toByteArray(object);
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }
    }

    public static byte[] encrypt(byte[] insecure, ExternalContext externalContext)
    {
        Assert.notNull(externalContext, "externalContext");

        testConfiguration(externalContext);

        try
        {
            Mac mac = createMac(externalContext);
            Cipher cipher = createCipher(externalContext, Cipher.ENCRYPT_MODE);

            //EtM (Encrypt-then-MAC) Composition Approach
            int macLenght = mac.getMacLength();
            byte[] secure = new byte[cipher.getOutputSize(insecure.length) + macLenght];
            int secureCount = cipher.doFinal(insecure, 0, insecure.length, secure);
            mac.update(secure, 0, secureCount);
            mac.doFinal(secure, secureCount);
                        
            return secure;
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
    }

    public static final byte[] compress(byte[] bytes)
    {
        
        try
        {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
            {
                try (GZIPOutputStream gzip = new GZIPOutputStream(baos))
                {
                    gzip.write(bytes, 0, bytes.length);
                    gzip.finish();
                    gzip.flush();

                    return baos.toByteArray();
                }
            }
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }
    }

    public static final byte[] encode(byte[] bytes)
    {
        return Base64.getEncoder().encode(bytes);
    }

    /**
     * This fires during the Restore View phase, restoring state.
     */
    public static final Object reconstruct(String string, ExternalContext ctx)
    {
        byte[] bytes;
        try
        {
            if (log.isLoggable(Level.FINE))
            {
                log.fine("Processing state : " + string);
            }

            bytes = string.getBytes(ZIP_CHARSET);
            bytes = decode(bytes);
            if (isSecure(ctx))
            {
                bytes = decrypt(bytes, ctx);
            }
            if (enableCompression(ctx))
            {
                bytes = decompress(bytes);
            }

            return getAsObject(bytes, ctx);
        }
        catch (Throwable e)
        {
            if (log.isLoggable(Level.FINE))
            {
                log.log(Level.FINE, "View State cannot be reconstructed", e);
            }
            return null;
        }
    }

    public static final byte[] decode(byte[] bytes)
    {
        return Base64.getDecoder().decode(bytes);
    }

    public static final byte[] decompress(byte[] bytes)
    {
        Assert.notNull(bytes, "bytes");

        try
        {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
            {
                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes))
                {
                    try (GZIPInputStream gis = new GZIPInputStream(bais))
                    {
                        byte[] buffer = new byte[bytes.length];
                        int length;
                        while ((length = gis.read(buffer)) != -1)
                        {
                            baos.write(buffer, 0, length);
                        }
                    }
                }

                return baos.toByteArray();
            }
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }
    }
    
    public static byte[] decrypt(byte[] secure, ExternalContext externalContext)
    {
        Assert.notNull(externalContext, "externalContext");

        testConfiguration(externalContext);

        try
        {
            Mac mac = createMac(externalContext);
            Cipher cipher = createCipher(externalContext, Cipher.DECRYPT_MODE);

            //EtM (Encrypt-then-MAC) Composition Approach
            int macLenght = mac.getMacLength();
            mac.update(secure, 0, secure.length - macLenght);
            byte[] signedDigestHash = mac.doFinal();

            boolean isMacEqual = true;
            for (int i = 0; i < signedDigestHash.length; i++)
            {
                if (signedDigestHash[i] != secure[secure.length - macLenght + i])
                {
                    isMacEqual = false;
                    // MYFACES-2934 Must compare *ALL* bytes of the hash, 
                    // otherwise a side-channel timing attack is theorically possible
                    // but with a very very low probability, because the
                    // comparison time is too small to be measured compared to
                    // the overall request time and in real life applications,
                    // there are too many uncertainties involved.
                    //break;
                }
            }
            if (!isMacEqual)
            {
                throw new ViewExpiredException();
            }
            
            return cipher.doFinal(secure, 0, secure.length - macLenght);
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
    }

    /**
     * Performs deserialization with the serialization provider created from the
     * SerialFactory.
     * 
     * @param bytes
     * @param ctx
     * @return
     */
    public static final Object getAsObject(byte[] bytes, ExternalContext ctx)
    {
        // get the Factory that was instantiated @ startup
        SerialFactory serialFactory = (SerialFactory) ctx.getApplicationMap().get(SERIAL_FACTORY);
        Assert.notNull(serialFactory, "serialFactory");

        try
        {
            return serialFactory.toObject(bytes);
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
    }

    /**
     * Utility method for generating base 64 encoded strings.
     * 
     * @param args
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws UnsupportedEncodingException
    {
        byte[] bytes = encode(args[0].getBytes(ZIP_CHARSET));
        System.out.println(new String(bytes, ZIP_CHARSET));
    }

    private static byte[] findInitializationVector(ExternalContext ctx)
    {
        byte[] iv = null;
        String ivString = ctx.getInitParameter(INIT_ALGORITHM_IV);

        if (ivString != null)
        {
            iv = decode(ivString.getBytes());
        }
        
        return iv;
    }

    private static String findAlgorithmParams(ExternalContext ctx)
    {
        String algorithmParams = ctx.getInitParameter(INIT_ALGORITHM_PARAM);        
        if (algorithmParams == null)
        {
            algorithmParams = DEFAULT_ALGORITHM_PARAMS;
        }
        
        if (log.isLoggable(Level.FINE))
        {
            log.fine("Using algorithm paramaters " + algorithmParams);
        }
        
        return algorithmParams;
    }

    private static String findAlgorithm(ExternalContext ctx)
    {
        String algorithm = ctx.getInitParameter(INIT_ALGORITHM);

        return findAlgorithm( algorithm );
    }
    
    private static String findAlgorithm(ServletContext ctx)
    {
        String algorithm = ctx.getInitParameter(INIT_ALGORITHM);

        return findAlgorithm( algorithm );
    }
    
    private static String findAlgorithm(String initParam)
    {
        if (initParam == null)
        {
            initParam = DEFAULT_ALGORITHM;
        }
        
        if (log.isLoggable(Level.FINE))
        {
            log.fine("Using algorithm " + initParam);
        }
        
        return initParam;
    }

    /**
     * Does nothing if the user has disabled the SecretKey cache. This is
     * useful when dealing with a JCA provider whose SecretKey 
     * implementation is not thread safe.
     * 
     * Instantiates a SecretKey instance based upon what the user has 
     * specified in the deployment descriptor.  The SecretKey is then 
     * stored in application scope where it can be used for all requests.
     */
    public static void initSecret(ServletContext servletContext)
    {
        Assert.notNull(servletContext, "servletContext");
        
        if (log.isLoggable(Level.FINE))
        {
            log.fine("Storing SecretKey @ " + INIT_SECRET_KEY_CACHE);
        }

        // Create and store SecretKey on application scope
        String cache = servletContext.getInitParameter(INIT_SECRET_KEY_CACHE);
        if (!"false".equals(cache))
        {
            String algorithm = findAlgorithm(servletContext);
            // you want to create this as few times as possible
            servletContext.setAttribute(INIT_SECRET_KEY_CACHE, new SecretKeySpec(
                    findSecret(servletContext, algorithm), algorithm));
        }

        if (log.isLoggable(Level.FINE))
        {
            log.fine("Storing SecretKey @ " + INIT_MAC_SECRET_KEY_CACHE);
        }
        
        String macCache = servletContext.getInitParameter(INIT_MAC_SECRET_KEY_CACHE);
        if (!"false".equals(macCache))
        {
            String macAlgorithm = findMacAlgorithm(servletContext);
            // init mac secret and algorithm 
            servletContext.setAttribute(INIT_MAC_SECRET_KEY_CACHE, new SecretKeySpec(
                    findMacSecret(servletContext, macAlgorithm), macAlgorithm));
        }
    }
    
    private static SecretKey getSecret(ExternalContext ctx)
    {
        Object secretKey = (SecretKey) ctx.getApplicationMap().get(INIT_SECRET_KEY_CACHE);
        
        if (secretKey == null)
        {
            String cache = ctx.getInitParameter(INIT_SECRET_KEY_CACHE);
            if ("false".equals(cache))
            {
                // No cache is used. This option is activated
                String secret = ctx.getInitParameter(INIT_SECRET);
                if (secret == null)
                {
                    throw new NullPointerException("Could not find secret using key '" + INIT_SECRET + '\'');
                }
                
                String algorithm = findAlgorithm(ctx);
                
                secretKey = new SecretKeySpec(findSecret(ctx, algorithm), algorithm);
            }
            else
            {
                throw new NullPointerException("Could not find SecretKey in application scope using key '" 
                        + INIT_SECRET_KEY_CACHE + '\'');
            }
        }
        
        if (!(secretKey instanceof SecretKey))
        {
            throw new ClassCastException("Did not find an instance of SecretKey "
                    + "in application scope using the key '" + INIT_SECRET_KEY_CACHE + '\'');
        }

        return (SecretKey) secretKey;
    }

    private static byte[] findSecret(ExternalContext ctx, String algorithm)
    {
        String secret = ctx.getInitParameter(INIT_SECRET);

        return findSecret(secret, algorithm);
    }    
    
    private static byte[] findSecret(ServletContext ctx, String algorithm)
    {
        String secret = ctx.getInitParameter(INIT_SECRET);

        return findSecret(secret, algorithm);
    }
    
    private static byte[] findSecret(String secret, String algorithm)
    {
        byte[] bytes = null;
        
        if (secret == null)
        {
            try
            {
                KeyGenerator kg = KeyGenerator.getInstance(algorithm);
                bytes = kg.generateKey().getEncoded();
                
                if (log.isLoggable(Level.FINE))
                {
                    log.fine("generated random password of length " + bytes.length);
                }
            }
            catch (NoSuchAlgorithmException e)
            {
                // Generate random password length 16,
                int length = 16;
                bytes = new byte[length];
                new Random().nextBytes(bytes);
                
                if (log.isLoggable(Level.FINE))
                {
                    log.fine("generated random password of length " + length);
                }
            }
        }
        else 
        {
            bytes = decode(secret.getBytes());
        }
        
        return bytes;
    }

    private static String findMacAlgorithm(ExternalContext ctx)
    {
        String algorithm = ctx.getInitParameter(INIT_MAC_ALGORITHM);

        return findMacAlgorithm(algorithm);
    }
    
    private static String findMacAlgorithm(ServletContext ctx)
    {
        String algorithm = ctx.getInitParameter(INIT_MAC_ALGORITHM);

        return findMacAlgorithm(algorithm);
    }
    
    private static String findMacAlgorithm(String initParam)
    {
        if (initParam == null)
        {
            initParam = DEFAULT_MAC_ALGORITHM;
        }
        
        if (log.isLoggable(Level.FINE))
        {
            log.fine("Using algorithm " + initParam);
        }
        
        return initParam;
    }
    
    private static SecretKey getMacSecret(ExternalContext ctx)
    {
        Object secretKey = (SecretKey) ctx.getApplicationMap().get(INIT_MAC_SECRET_KEY_CACHE);
        
        if (secretKey == null)
        {
            String cache = ctx.getInitParameter(INIT_MAC_SECRET_KEY_CACHE);
            if ("false".equals(cache))
            {
                // No cache is used. This option is activated
                String secret = ctx.getInitParameter(INIT_MAC_SECRET);
                if (secret == null)
                {
                    throw new NullPointerException("Could not find secret using key '" + INIT_MAC_SECRET + '\'');
                }
                
                String macAlgorithm = findMacAlgorithm(ctx);

                secretKey = new SecretKeySpec(findMacSecret(ctx, macAlgorithm), macAlgorithm);
            }
            else
            {
                throw new NullPointerException("Could not find SecretKey in application scope using key '" 
                        + INIT_MAC_SECRET_KEY_CACHE + '\'');
            }
        }
        
        if (!(secretKey instanceof SecretKey))
        {
            throw new ClassCastException("Did not find an instance of SecretKey "
                    + "in application scope using the key '" + INIT_MAC_SECRET_KEY_CACHE + '\'');
        }

        return (SecretKey) secretKey;
    }

    private static byte[] findMacSecret(ExternalContext ctx, String algorithm)
    {
        String secret = ctx.getInitParameter(INIT_MAC_SECRET);

        return findMacSecret(secret, algorithm);
    }    
    
    private static byte[] findMacSecret(ServletContext ctx, String algorithm)
    {
        String secret = ctx.getInitParameter(INIT_MAC_SECRET);

        return findMacSecret(secret, algorithm);
    }

    private static byte[] findMacSecret(String secret, String algorithm)
    {
        byte[] bytes = null;
        
        if (secret == null)
        {
            try
            {
                KeyGenerator kg = KeyGenerator.getInstance(algorithm);
                bytes = kg.generateKey().getEncoded();
                
                if (log.isLoggable(Level.FINE))
                {
                    log.fine("generated random mac password of length " + bytes.length);
                }
            }
            catch (NoSuchAlgorithmException e)
            {
                // Generate random password length 8, 
                int length = 8;
                bytes = new byte[length];
                new Random().nextBytes(bytes);
                
                if(log.isLoggable(Level.FINE))
                {
                    log.fine("generated random mac password of length " + length);
                }
            }
        }
        else 
        {
            bytes = decode(secret.getBytes());
        }
        
        return bytes;
    }
}
