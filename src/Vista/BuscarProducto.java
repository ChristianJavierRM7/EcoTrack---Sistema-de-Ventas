package Vista;

import Controladores.ProductosJpaController;
import Entidades.Productos;
import jakarta.persistence.Persistence;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class BuscarProducto extends javax.swing.JDialog {
    private Sistema sistema;
private ProductosJpaController productosJpa;
private List<Productos> listaProductos;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(BuscarProducto.class.getName());


    public BuscarProducto(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        TableResultados.setDefaultEditor(Object.class, null);  

    }

  public BuscarProducto(java.awt.Frame parent, boolean modal, Sistema sistema, String nombre) {
    super(parent, modal);
    initComponents();
    this.sistema = sistema;
TableResultados.setDefaultEditor(Object.class, null);  

    productosJpa = new ProductosJpaController(
            Persistence.createEntityManagerFactory("SistemaVentaPU")
    );

    setLocationRelativeTo(parent);
    listarResultados(nombre);
}


   private void listarResultados(String nombre) {

    listaProductos = productosJpa.buscarPorNombre(nombre);

    DefaultTableModel model = (DefaultTableModel) TableResultados.getModel();
    model.setRowCount(0);

    for (Productos p : listaProductos) {
        model.addRow(new Object[]{
            p.getCodigo(),
            p.getNombre(),
            p.getPrecio(),
            p.getStock()
        });
    }
}

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TableResultados = new javax.swing.JTable();
        jLabel34 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel66 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(204, 255, 204));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TableResultados.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        TableResultados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Código", "Nombre", "Precio", "Stock"
            }
        ));
        TableResultados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableResultadosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(TableResultados);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 850, 390));

        jLabel34.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel34.setText("Busqueda de productos");
        jPanel1.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 390, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Carrito-de-compras.png"))); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 40, 40, 40));

        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel1.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 390, 10));

        jLabel66.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel66.setText("* Haga doble click en el producto que desea agregar a la venta:");
        jPanel1.add(jLabel66, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 50, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 490));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TableResultadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableResultadosMouseClicked

    if (evt.getClickCount() == 2) {

        int fila = TableResultados.getSelectedRow();
        if (fila == -1) return;

        String codigo = TableResultados.getValueAt(fila, 0).toString();
        String nombre = TableResultados.getValueAt(fila, 1).toString();
        String precio = TableResultados.getValueAt(fila, 2).toString();
        String stock  = TableResultados.getValueAt(fila, 3).toString(); 

        sistema.cargarProductoDesdeBusqueda(codigo, nombre, precio, stock);

        dispose();
    }
    }//GEN-LAST:event_TableResultadosMouseClicked

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                BuscarProducto dialog = new BuscarProducto(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable TableResultados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator3;
    // End of variables declaration//GEN-END:variables
}
