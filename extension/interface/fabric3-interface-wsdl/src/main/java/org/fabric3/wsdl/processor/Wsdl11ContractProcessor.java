/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.wsdl.processor;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.xsd.XSDComplexType;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 * WSDL 1.1 processor implementation.
 */
public class Wsdl11ContractProcessor implements WsdlContractProcessor {

    public WsdlServiceContract introspect(PortType portType, Definition definition, XmlSchemaCollection collection, IntrospectionContext context) {
        List<Operation> operations = new LinkedList<Operation>();
        for (Object object : portType.getOperations()) {
            javax.wsdl.Operation wsdlOperation = (javax.wsdl.Operation) object;
            Operation operation = createOperation(wsdlOperation, collection, portType, context);
            operations.add(operation);
        }
        WsdlServiceContract contract = new WsdlServiceContract(portType, definition);
        contract.setOperations(operations);
        return contract;
    }

    /**
     * Creates a operation model object from a WSDL operation.
     *
     * @param operation  the WSDL operation
     * @param collection parsed schema included in or referenced by the WSDL
     * @param portType   the port type being introspected
     * @param context    the introspection context
     * @return the operation model object
     */
    private Operation createOperation(javax.wsdl.Operation operation, XmlSchemaCollection collection, PortType portType, IntrospectionContext context) {
        Input input = operation.getInput();
        Message message = input.getMessage();
        List<DataType<?>> inputTypes = getInputTypes(message, collection, portType, context);

        Map faults = operation.getFaults();
        List<DataType<?>> faultTypes = getFaultTypes(faults, collection, portType, context);

        Output output = operation.getOutput();
        DataType<?> outputType = getOutputType(output, collection, portType, context);

        String name = operation.getName();
        Operation op = new Operation(name, inputTypes, outputType, faultTypes);
        op.setRemotable(true);
        return op;
    }

    @SuppressWarnings({"unchecked"})
    private List<DataType<?>> getInputTypes(Message message, XmlSchemaCollection collection, PortType portType, IntrospectionContext context) {
        List<DataType<?>> types = new ArrayList<DataType<?>>();
        // Note Message.getParts() may not return the parts in proper order; Message.getOrderedParts(null) does
        List parts = message.getOrderedParts(null);
        if (parts.isEmpty()) {
            DataType<?> type = getElementDataType(message.getQName(), collection, portType, context);
            types.add(type);
        } else {
            for (Part part : (Collection<Part>) parts) {
                XSDType dataType = getDataType(part, collection, portType, context);
                if (dataType != null) {
                    types.add(dataType);
                }
            }
        }
        return types;
    }

    @SuppressWarnings("unchecked")
    private List<DataType<?>> getFaultTypes(Map faults, XmlSchemaCollection collection, PortType portType, IntrospectionContext context) {
        List<DataType<?>> types = new LinkedList<DataType<?>>();
        for (Fault fault : (Collection<Fault>) faults.values()) {
            Part part = (Part) fault.getMessage().getOrderedParts(null).get(0);
            XSDType dataType = getDataType(part, collection, portType, context);
            if (dataType != null) {
                types.add(dataType);
            }
        }
        return types;

    }

    private DataType<?> getOutputType(Output output, XmlSchemaCollection collection, PortType portType, IntrospectionContext context) {
        if (output == null) {
            // no output type specified (e.g. one-way operation), use void
            return new XSDSimpleType(Void.TYPE, new QName("void"));
        }
        Message message = output.getMessage();
        List parts = message.getOrderedParts(null);
        if (parts.isEmpty()) {
            return getElementDataType(message.getQName(), collection, portType, context);
        } else {
            Part part = (Part) parts.get(0);
            return getDataType(part, collection, portType, context);
        }
    }

    private XSDType getDataType(Part part, XmlSchemaCollection collection, PortType portType, IntrospectionContext context) {
        QName elementName = part.getElementName();
        XSDType dataType = null;
        QName typeName = part.getTypeName();
        if (elementName != null) {
            dataType = getElementDataType(elementName, collection, portType, context);
        } else if (typeName != null) {
            dataType = getSchemaDataType(typeName, collection, portType, context);
        }
        return dataType;
    }

    private XSDType getElementDataType(QName elementName, XmlSchemaCollection collection, PortType portType, IntrospectionContext context) {
        XmlSchemaElement element = collection.getElementByQName(elementName);
        if (element == null) {
            SchemaTypeNotFound error = new SchemaTypeNotFound("Schema type " + elementName + " not found referenced in: " + portType.getQName());
            context.addError(error);
            return null;
        }
        XmlSchemaType type = element.getSchemaType();
        if (type == null) {
            SchemaTypeNotFound error = new SchemaTypeNotFound("Invalid schema type " + elementName + " referenced in: " + portType.getQName());
            context.addError(error);
            return null;
        }
        return createDataType(type, elementName);
    }

    private XSDType getSchemaDataType(QName typeName, XmlSchemaCollection collection, PortType portType, IntrospectionContext context) {
        XmlSchemaType type = collection.getTypeByQName(typeName);
        if (type == null) {
            SchemaTypeNotFound error = new SchemaTypeNotFound("Schema type " + typeName + " not found referenced in: " + portType.getQName());
            context.addError(error);
            return null;
        }
        return createDataType(type, null);
    }

    private XSDType createDataType(XmlSchemaType type, QName elementName) {
        QName name = type.getQName();
        if (name == null) {
            name = elementName;
        }
        if (type instanceof XmlSchemaComplexType) {
            return introspectComplexType((XmlSchemaComplexType) type, name);
        } else if (type instanceof XmlSchemaSimpleType) {
            return new XSDSimpleType(Object.class, name);
        } else {
            // should not happen
            throw new AssertionError("Unknown Schema type" + type);
        }
    }

    @SuppressWarnings({"unchecked"})
    private XSDType introspectComplexType(XmlSchemaComplexType type, QName name) {
        if (type.getParticle() instanceof XmlSchemaSequence) {
            XmlSchemaSequence sequence = (XmlSchemaSequence) type.getParticle();
            List<XSDType> sequenceTypes = new ArrayList<XSDType>();
            Iterator<XmlSchemaObject> iter = sequence.getItems().getIterator();
            while (iter.hasNext()) {
                XmlSchemaObject o = iter.next();
                if (o instanceof XmlSchemaElement) {
                    XmlSchemaElement schemaElement = (XmlSchemaElement) o;
                    QName schemaTypeName = schemaElement.getSchemaTypeName();
                    XmlSchemaType schemaType = schemaElement.getSchemaType();
                    if (schemaType instanceof XmlSchemaComplexType) {
                        sequenceTypes.add(new XSDComplexType(Object.class, schemaTypeName));
                    } else if (schemaType instanceof XmlSchemaSimpleType) {
                        sequenceTypes.add(new XSDSimpleType(Object.class, schemaTypeName));
                    }
                } else if (o instanceof XmlSchemaType) {
                    XmlSchemaType schemaType = (XmlSchemaType) o;
                    QName schemaTypeName = schemaType.getQName();
                    if (schemaType instanceof XmlSchemaComplexType) {
                        sequenceTypes.add(new XSDComplexType(Object.class, schemaTypeName));
                    } else if (schemaType instanceof XmlSchemaSimpleType) {
                        sequenceTypes.add(new XSDSimpleType(Object.class, schemaTypeName));
                    }
                }
            }
            return new XSDComplexType(Object.class, name, sequenceTypes);
        }
        return new XSDComplexType(Object.class, name);
    }

}