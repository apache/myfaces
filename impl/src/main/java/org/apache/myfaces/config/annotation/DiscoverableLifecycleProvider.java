package org.apache.myfaces.config.annotation;

/**
 * Created by IntelliJ IDEA.
 * User: bommel
 * Date: Mar 13, 2007
 * Time: 10:09:38 PM
 */
public interface DiscoverableLifecycleProvider extends LifecycleProvider
{
    public boolean isAvailable();
}
