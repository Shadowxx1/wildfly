/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.clustering.cluster.ejb3.deployment;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.as.test.clustering.ClusteringTestConstants;
import org.jboss.as.test.clustering.cluster.ClusterAbstractTestCase;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The purpose of this testcase is to ensure that a EJB marked as clustered, either via annotation or deployment descriptor,
 * deploys successfully. This test does <b>not</b> check any clustering semantics (like failover, replication, etc...)
 *
 * @author Jaikiran Pai
 */
@RunWith(Arquillian.class)
public class ClusteredBeanDeploymentTestCase extends ClusterAbstractTestCase {

    private static final String DD_BASED_MODULE_NAME = "clustered-ejb-deployment";

    @Deployment(name = DEPLOYMENT_1, managed = false)
    @TargetsContainer(CONTAINER_1)
    public static Archive<?> createDDBasedDeployment() {
        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, DD_BASED_MODULE_NAME + ".jar");
        ejbJar.addClasses(DDBasedClusteredBean.class, ClusteredBean.class);
        ejbJar.addClasses(ClusterAbstractTestCase.class, ClusteringTestConstants.class);
        ejbJar.addAsManifestResource("cluster/ejb3/deployment/jboss-ejb3.xml", "jboss-ejb3.xml");
        return ejbJar;
    }

    // TODO: Workaround for https://issues.jboss.org/browse/ARQ-351, remove after fixed.

    @Override
    public void beforeTestMethod() {
        // Do nothing.
    }

    @Override
    public void afterTestMethod() {
        // Do nothing.
    }

    @Test
    @InSequence(Integer.MIN_VALUE)
    @RunAsClient
    public void setup() {
        start(CONTAINER_1);
        deploy(DEPLOYMENT_1);
    }

    @Test
    @InSequence(Integer.MAX_VALUE)
    @RunAsClient
    public void cleanup() {
        start(CONTAINER_1);
        undeploy(DEPLOYMENT_1);
    }

    /**
     * Test that a bean marked as clustered via deployment descriptor, deploys fine and is invokable. This test does <b>not</b>
     * test any clustering semantics (like failover etc...)
     *
     * @throws Exception
     */
    @Test
    public void testDDBasedClusteredBeanDeployment() throws Exception {
        final Context ctx = new InitialContext();
        final DDBasedClusteredBean ddBasedClusteredBean = (DDBasedClusteredBean) ctx.lookup("java:module/" + DDBasedClusteredBean.class.getSimpleName() + "!" + DDBasedClusteredBean.class.getName());
        final int NUM_TIMES = 5;
        for (int i = 0; i < NUM_TIMES; i++) {
            ddBasedClusteredBean.increment();
        }
        Assert.assertEquals("Unexpected count on stateful bean", ddBasedClusteredBean.getCount(), NUM_TIMES);
    }

    /**
     * Test that a bean marked as clustered via annotation, deploys fine and is invokable. This test does <b>not</b> test any
     * clustering semantics (like failover etc...)
     *
     * @throws Exception
     */
    @Test
    public void testAnnotationBasedClusteredBeanDeployment() throws Exception {
        final Context ctx = new InitialContext();
        final ClusteredBean clusteredBean = (ClusteredBean) ctx.lookup("java:module/" + ClusteredBean.class.getSimpleName() + "!" + ClusteredBean.class.getName());
        final int NUM_TIMES = 5;
        for (int i = 0; i < NUM_TIMES; i++) {
            clusteredBean.increment();
        }
        Assert.assertEquals("Unexpected count on stateful bean", clusteredBean.getCount(), NUM_TIMES);
    }
}
