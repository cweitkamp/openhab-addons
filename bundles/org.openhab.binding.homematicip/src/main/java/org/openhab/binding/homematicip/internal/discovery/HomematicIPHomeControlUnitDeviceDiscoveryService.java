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
import org.openhab.binding.homematicip.internal.handler.HomematicIPHomeControlUnitHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link HomematicIPHomeControlUnitDeviceDiscoveryService} creates Things based on the found devices on the Home
 * Control Unit.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = HomematicIPHomeControlUnitDeviceDiscoveryService.class)
@NonNullByDefault
public class HomematicIPHomeControlUnitDeviceDiscoveryService extends
        AbstractHomematicIPDeviceDiscoveryService<HomematicIPHomeControlUnitHandler> implements DiscoveryService {

    @Activate
    public HomematicIPHomeControlUnitDeviceDiscoveryService() {
        super(HomematicIPHomeControlUnitHandler.class);
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
