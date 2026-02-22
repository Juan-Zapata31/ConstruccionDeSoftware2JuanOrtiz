package app.domain.services;

import app.domain.models.Order;
import app.domain.models.OrderItem;
import app.domain.models.ItemType;
import app.domain.ports.OrderPort;
import app.domain.Exceptions.BusinessException;

// Servicio para crear órdenes y aplicar reglas de negocio relacionadas
public class CreateOrders {

    private OrderPort orderPort;

    // Constructor: recibe el puerto de persistencia de órdenes
    public CreateOrders (OrderPort orderPort) {
        this.orderPort = orderPort;
    }

    // Punto de entrada para crear una orden: valida y persiste
    public void createOrder(Order order) throws BusinessException {
        validateOrderRules(order);
        orderPort.save(order);
    }
    
    // Valida todas las reglas de negocio que deben cumplirse antes de guardar
    private void validateOrderRules(Order order) throws BusinessException {
        // Verificar existencia por número de orden
        if (orderPort.existsByNumber(String.valueOf(order.getId()))) {
            throw new BusinessException("Ya existe una orden con ese numero");
        }
        
        // Regla numero 1: Si hay ayuda diagnostica, no puede haber medicamentos ni procedimientos.
        validateDiagnosticExclusivityHelp(java.util.Arrays.asList(order.getOrderItems()));

        // Regla numero 6: NO puede haber items duplicados 
        validateNoDuplicateItems(order.getOrderItems());
    }
    
    // Asegura que no coexistan ayuda diagnóstica con medicamentos o procedimientos
    private void validateDiagnosticExclusivityHelp(java.util.List<OrderItem> orderItems) throws BusinessException {
        boolean hasDiagnosticHelp = false;
        boolean hasMedicationOrProcedure = false;

        for (OrderItem item : orderItems) {
            // Marcar si hay ayuda diagnóstica
            if (item.getItemType() == ItemType.MEDICALSUPPORT) {
                hasDiagnosticHelp = true;
            }
            // Marcar si hay medicamento o procedimiento
            if (item.getItemType() == ItemType.MEDICINE || item.getItemType() == ItemType.PROCEDURE) {
                hasMedicationOrProcedure = true;
            }
        }
        // Si hay ambos tipos, la regla se viola
        if (hasDiagnosticHelp && hasMedicationOrProcedure) {
            throw new BusinessException("No puede haber ayuda diagnostica junto con medicamentos o procedimientos");
        }
    }
    
    // Verifica que no existan ítems duplicados en la misma orden (comparando IDs)
    private void validateNoDuplicateItems(OrderItem[] items) throws BusinessException {
        for (int i = 0; i < items.length; i++) {
            for (int j = i + 1; j < items.length; j++) {
                // Comparación simple por id del ítem
                if (items[i].getItem().getId() == items[j].getItem().getId()) {
                    throw new BusinessException("No puede haber ítems duplicados en la misma orden");
                }
            }
        }
    }
}