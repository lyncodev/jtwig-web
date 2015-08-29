package org.jtwig.web.resource.resolver;

import com.google.common.base.Optional;
import org.jtwig.resource.Resource;
import org.jtwig.resource.exceptions.ResourceException;
import org.jtwig.resource.resolver.ResourceResolver;
import org.jtwig.web.resource.WebResource;
import org.jtwig.web.servlet.ServletRequestHolder;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class WebResourceResolver implements ResourceResolver {
    public static final String PREFIX = "web:";

    @Override
    public Optional<Resource> resolve(Resource resource, String relativePath) {
        if (relativePath.startsWith(PREFIX)) {
            relativePath = relativePath.substring(PREFIX.length());
        }

        File relativeFile = new File(relativePath);
        if(!relativeFile.isAbsolute()) {
            if(resource instanceof WebResource) {
                File parentFile = ((WebResource)resource).getFile().getParentFile();
                File file = new File(parentFile, relativePath);
                return this.resolve(file);
            } else {
                return Optional.absent();
            }
        } else {
            return this.resolve(relativeFile);
        }
    }

    private Optional<Resource> resolve(File relativeFile) {
        try {
            Optional<URL> optional = Optional.fromNullable(getServletContext().getResource(relativeFile.getPath()));
            if (optional.isPresent()) {
                return Optional.<Resource>of(new WebResource(relativeFile.getPath()));
            } else {
                return Optional.absent();
            }
        } catch (MalformedURLException e) {
            throw new ResourceException(String.format("Unable to resolve URL from file %s", relativeFile), e);
        }
    }

    protected ServletContext getServletContext() {
        return ServletRequestHolder.get().getServletContext();
    }

}