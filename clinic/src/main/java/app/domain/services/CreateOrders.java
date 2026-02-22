package app.domain.services;

import app.domain.models.Order;
import app.domain.models.OrderItem;
import app.domain.models.ItemType;
import app.domain.ports.OrderPort;
import app.domain.Exceptions.BusinessException;

public class CreateOrders {

    private OrderPort orderPort;

    public CreateOrders (OrderPort orderPort) {
        this.orderPort = orderPort;
    }

    public void createOrder(Order order) throws BusinessException {
        validateOrderRules(order);
        orderPort.save(order);
    }
    
    private void validateOrderRules(Order order) throws BusinessException {
        if (orderPort.existsByNumber(String.valueOf(order.getId()))) {
            throw new BusinessException("Ya existe una orden con ese numero");
        }
        
        // Regla numero 1: Si hay ayuda diagnostica, no puede haber medicamentos ni procedimientos
        validateDiagnosticExclusivityHelp(java.util.Arrays.asList(order.getOrderItems()));

        // Regla numero 6: NO puede haber items duplicados 
        validateNoDuplicateItems(order.getOrderItems());
    }
    
    private void validateDiagnosticExclusivityHelp(java.util.List<OrderItem> orderItems) throws BusinessException {
        boolean hasDiagnosticHelp = false;
        boolean hasMedicationOrProcedure = false;

        for (OrderItem item : orderItems) {
            if (item.getItemType() == ItemType.MEDICALSUPPORT) {
                hasDiagnosticHelp = true;
            }
            if (item.getItemType() == ItemType.MEDICINE || item.getItemType() == ItemType.PROCEDURE) {
                hasMedicationOrProcedure = true;
            }
        }
        if (hasDiagnosticHelp && hasMedicationOrProcedure) {
            throw new BusinessException("No puede haber ayuda diagnostica junto con medicamentos o procedimientos");
        }
    }
    
    private void validateNoDuplicateItems(OrderItem[] items) throws BusinessException {
        for (int i = 0; i < items.length; i++) {
            for (int j = i + 1; j < items.length; j++) {
                if (items[i].getItem().getId() == items[j].getItem().getId()) {
                    throw new BusinessException("No puede haber ítems duplicados en la misma orden");
                }
            }
        }
    }
}