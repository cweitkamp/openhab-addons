/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.homematicip.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homematicip.internal.handler.HomematicIPAccessPointHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link HomematicIPAccessPointDeviceDiscoveryService} creates Things based on the found devices on the Access
 * Point.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = HomematicIPAccessPointDeviceDiscoveryService.class)
@NonNullByDefault
public class HomematicIPAccessPointDeviceDiscoveryService
        extends AbstractHomematicIPDeviceDiscoveryService<HomematicIPAccessPointHandler> implements DiscoveryService {

    @Activate
    public HomematicIPAccessPointDeviceDiscoveryService() {
        super(HomematicIPAccessPointHandler.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        thingHandler.registerDiscoveryService(this);
    }

    @Override
    public void dispose() {
        thingHandler.unregisterDiscoveryListener();
        super.dispose();
    }

    @Override
    public void startScan() {
        logger.debug("Start manual homematic IP devices scan.");
        thingHandler.refreshData();
    }
}
