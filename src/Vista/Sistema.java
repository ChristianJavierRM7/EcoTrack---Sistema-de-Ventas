package Vista;

import Controladores.ClientesJpaController;
import Controladores.ProveedorJpaController;
import Entidades.Clientes;
import Entidades.Proveedor;
import Controladores.ProductosJpaController;
import Entidades.Detalle;
import Controladores.DetalleJpaController;
import Modelo.Eventos;
import Entidades.Productos;
import Modelo.Venta;
import Reportes.Excel;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import Modelo.VentaDao;
import Reportes.Grafico;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.itextpdf.text.pdf.PdfPCell;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import javax.swing.JTable;
import Entidades.Config;
import Controladores.ConfigJpaController;
import Controladores.PerdidasJpaController;
import Entidades.Ventas;
import Controladores.VentasJpaController;
import Entidades.Perdidas;
import Entidades.Usuarios;
import java.awt.Color;
import java.awt.Component;
import javax.swing.table.DefaultTableCellRenderer;


public class Sistema extends javax.swing.JFrame {


    Date fechaVenta = new Date();
    String fechaActual = new SimpleDateFormat("dd-MM-yyyy").format(fechaVenta);
    Venta v = new Venta();
    VentaDao Vdao = new VentaDao();
  
    Config conf= new Config();
    Eventos event = new Eventos();
    DefaultTableModel modelo = new DefaultTableModel();
    DefaultTableModel tmp = new DefaultTableModel();

    int item;
    double Totalpagar = 0.00;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Sistema.class.getName());
    private ClientesJpaController clientesJpa;
    private ProveedorJpaController proveedorJpa;
    private ProductosJpaController productosJpa;
    private ConfigJpaController configJpa;
    private VentasJpaController ventasJpa;
    private DetalleJpaController detalleJpa;
    private PerdidasJpaController perdidasJpa;
    
    
    public Sistema() {
        
        initComponents();
        setLocationRelativeTo(null);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SistemaVentaPU");
    this.clientesJpa = new ClientesJpaController(emf);  
    this.proveedorJpa = new ProveedorJpaController(emf);
    this.productosJpa = new ProductosJpaController(emf);
    this.configJpa = new ConfigJpaController(emf);
    this.ventasJpa = new VentasJpaController(emf);
    this.detalleJpa = new DetalleJpaController(emf);
    this.perdidasJpa = new PerdidasJpaController(emf);
    CrearConfigInicial();
txtIdCliente.setVisible(false);
txtIdpro.setVisible(false);
        jTabbedPane1.setEnabled(false);
        txtRazonCV.setVisible(false);
        txtIdVenta.setVisible(false);
        txtIdPro.setVisible(false);
        txtIdProveedor.setVisible(false);
        txtTelefonoCV.setVisible(false);
        txtDireccionCV.setVisible(false);
        txtVendedor.setVisible(false);
        AutoCompleteDecorator.decorate(cbxProveedorPro);
cargarProveedoresEnCombo();
        txtIdConfig.setVisible(false);
    }

     public Sistema(Usuarios usuario) {
    initComponents();
    setLocationRelativeTo(null);
    jTabbedPane1.setSelectedIndex(6);
    jTabbedPane1.setEnabled(false);
   
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("SistemaVentaPU");
    this.clientesJpa = new ClientesJpaController(emf);
    this.proveedorJpa = new ProveedorJpaController(emf);
    this.productosJpa = new ProductosJpaController(emf);
    this.configJpa = new ConfigJpaController(emf);
    this.ventasJpa = new VentasJpaController(emf);
    this.detalleJpa = new DetalleJpaController(emf);
    this.perdidasJpa = new PerdidasJpaController(emf);
    
    LabelVendedor.setText(usuario.getNombre());

  
    if (usuario.getRol().equals("Asistente")) {
        btnProductos.setEnabled(false);
        btnProveedor.setEnabled(false);
    }

 
    txtIdCliente.setVisible(false);
    txtIdVenta.setVisible(false);
    txtIdPro.setVisible(false);
    txtIdProveedor.setVisible(false);
    txtIdConfig.setVisible(false);
}
     
public void cargarProductoDesdeBusqueda(Productos p) {

    txtCodigoVenta.setText(p.getCodigo());
    txtDescripcionVenta.setText(p.getNombre());
    txtPrecioVenta.setText(p.getPrecio().toString());
    txtStockDisponible.setText(String.valueOf(p.getStock()));

    txtCantidadVenta.requestFocus();
}

   

private int RegistrarVentaJPA() {
    try {
        Ventas v = new Ventas();
        
        v.setCliente(txtNombreClienteventa.getText());
        v.setVendedor(txtVendedor.getText());
        v.setFecha(fechaActual);
        v.setTotal(BigDecimal.valueOf(Totalpagar));

        ventasJpa.create(v);

        return v.getId();  
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error al registrar venta: " + e.getMessage());
        return -1;
    }
}

private void ActualizarStockJPA(){
    for (int i = 0; i < TablaVenta.getRowCount(); i++) {
        String cod = TablaVenta.getValueAt(i, 0).toString().trim();
        int cant = Integer.parseInt(TablaVenta.getValueAt(i, 2).toString());

        try {
            Productos producto = productosJpa.findByCodigo(cod); 

            if (producto == null) {
                JOptionPane.showMessageDialog(this, "Producto con código " + cod + " no encontrado. Stock no actualizado.");
                continue;
            }

            int nuevoStock = producto.getStock() - cant;
            if (nuevoStock < 0) {
                JOptionPane.showMessageDialog(this, "Stock insuficiente para el producto " + producto.getNombre());
                continue;
            }

            producto.setStock(nuevoStock);

            try {
                productosJpa.edit(producto); 
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al actualizar stock del producto " + cod + ": " + e.getMessage());
            }

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida en la fila " + i);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado al actualizar stock: " + ex.getMessage());
        }
    }
}

private void RegistrarDetalleJPA(int idVenta) {

    for (int i = 0; i < TablaVenta.getRowCount(); i++) {

        Detalle d = new Detalle();

        d.setIdVenta(String.valueOf(idVenta));
        d.setCodPro(TablaVenta.getValueAt(i, 0).toString());
        d.setCantidad(TablaVenta.getValueAt(i, 2).toString());
        d.setPrecio(new BigDecimal(TablaVenta.getValueAt(i, 3).toString()));

        detalleJpa.create(d);
    }
}

       
 public void ListarCliente() {
    List<Clientes> lista = clientesJpa.findClientesEntities();
    modelo = (DefaultTableModel) TableCliente.getModel();
    Object[] ob = new Object[6];

    for (Clientes c : lista) {
        ob[0] = c.getId();
        ob[1] = c.getDni();
        ob[2] = c.getNombre();
        ob[3] = c.getTelefono();
        ob[4] = c.getDireccion();
        ob[5] = c.getRazon();
        modelo.addRow(ob);
    }
    TableCliente.setModel(modelo);
}

 private void cargarProveedoresEnCombo() {
    cbxProveedorPro.removeAllItems(); 

    List<Proveedor> lista = proveedorJpa.findProveedorEntities();

    for (Proveedor p : lista) {
        cbxProveedorPro.addItem(p.getNombre());
    }
}

 
public void ListarProveedor() {

    List<Proveedor> lista = proveedorJpa.findProveedorEntities();

    modelo = (DefaultTableModel) TableProveedor.getModel();
    modelo.setRowCount(0);

    Object[] ob = new Object[9];

    for (Proveedor p : lista) {
        ob[0] = p.getId();
        ob[1] = p.getRuc();
        ob[2] = p.getNombre();
        ob[3] = p.getTelefono();
        ob[4] = p.getDireccion();
        ob[5] = p.getRazon();
        ob[6] = p.getCorreo();
        ob[7] = p.getEstado();
        ob[8] = p.getObservaciones();

        modelo.addRow(ob);
    }

    TableProveedor.setModel(modelo);
}


    
public void ListarProductos() {

    List<Productos> lista = productosJpa.findProductosEntities();
    modelo = (DefaultTableModel) TableProducto.getModel();
    modelo.setRowCount(0);

    Object[] ob = new Object[10];

    for (Productos p : lista) {
        ob[0] = p.getId();
        ob[1] = p.getCodigo();
        ob[2] = p.getNombre();
        ob[3] = p.getProveedor();
        ob[4] = p.getStock();
        ob[5] = p.getPrecio();
        ob[6] = p.getCosto();
        ob[7] = p.getFechaElaboracion();
        ob[8] = p.getFechaCaducidad();
        ob[9] = p.getStockMin();

        modelo.addRow(ob);
    }

    TableProducto.setModel(modelo);
    
StringBuilder alerta = new StringBuilder();
for (Productos p : lista) {
    if (p.getStock() <= p.getStockMin()) {
        alerta.append("• ").append(p.getNombre())
              .append(" — Stock: ").append(p.getStock())
              .append(" / Mínimo: ").append(p.getStockMin())
              .append("\n");
    }
}

if (alerta.length() > 0) {
    JOptionPane.showMessageDialog(
        null,
        "⚠ PRODUCTOS CON STOCK BAJO:\n\n" + alerta.toString(),
        "Alerta de Stock",
        JOptionPane.WARNING_MESSAGE
    );
}


    TableProducto.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int stock = Integer.parseInt(table.getValueAt(row, 4).toString());
            int stockMin = Integer.parseInt(table.getValueAt(row, 9).toString());

            if (stock <= stockMin) {
                c.setBackground(Color.RED);
                c.setForeground(Color.WHITE);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    });
}
    
    public void ListarVentas()  {
        List<Venta> ListarVenta = Vdao.Listarventas();
        modelo = (DefaultTableModel) TableVentas.getModel();
        Object[] ob = new Object[4];
        for (int i = 0; i < ListarVenta.size(); i++) {
            ob[0] = ListarVenta.get(i).getId();
            ob[1] = ListarVenta.get(i).getCliente();
            ob[2] = ListarVenta.get(i).getVendedor();
            ob[3] = ListarVenta.get(i).getTotal();
            modelo.addRow(ob);
        }
        TableVentas.setModel(modelo);
    }   
       
   public void LimpiarTable(JTable tabla) {
    DefaultTableModel model = (DefaultTableModel) tabla.getModel();
    model.setRowCount(0);
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        LabelVendedor = new javax.swing.JLabel();
        btnNuevaVenta = new javax.swing.JButton();
        btnProveedor = new javax.swing.JButton();
        btnVentas = new javax.swing.JButton();
        btnConfig = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        btnNuevaVenta1 = new javax.swing.JButton();
        btnClientes = new javax.swing.JButton();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        btnProductos = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        btnEliminarventa = new javax.swing.JButton();
        txtCodigoVenta = new javax.swing.JTextField();
        txtDescripcionVenta = new javax.swing.JTextField();
        txtCantidadVenta = new javax.swing.JTextField();
        txtPrecioVenta = new javax.swing.JTextField();
        txtStockDisponible = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        TablaVenta = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        txtNombreClienteventa = new javax.swing.JTextField();
        txtRucVenta = new javax.swing.JTextField();
        btnGenerarVenta = new javax.swing.JButton();
        LabelTotal = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtTelefonoCV = new javax.swing.JTextField();
        txtDireccionCV = new javax.swing.JTextField();
        txtRazonCV = new javax.swing.JTextField();
        txtIdPro = new javax.swing.JTextField();
        btnGraficar = new javax.swing.JButton();
        Midate = new com.toedter.calendar.JDateChooser();
        jLabel35 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtVendedor = new javax.swing.JTextField();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        txtBuscarProducto = new javax.swing.JTextField();
        btnBuscarProducto = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtRazonCliente = new javax.swing.JTextField();
        txtDniCliente = new javax.swing.JTextField();
        txtTelefonoCliente = new javax.swing.JTextField();
        txtDireccionCliente = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        TableCliente = new javax.swing.JTable();
        btnEditarCliente = new javax.swing.JButton();
        btnGuardarCliente = new javax.swing.JButton();
        btnEliminarCliente = new javax.swing.JButton();
        btnNuevoCliente = new javax.swing.JButton();
        txtIdCliente = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        txtNombreCliente = new javax.swing.JTextField();
        jSeparator4 = new javax.swing.JSeparator();
        jPanel4 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        txtRazonProveedor = new javax.swing.JTextField();
        txtRucProveedor = new javax.swing.JTextField();
        txtNombreproveedor = new javax.swing.JTextField();
        txtTelefonoProveedor = new javax.swing.JTextField();
        txtDireccionProveedor = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        TableProveedor = new javax.swing.JTable();
        btnguardarProveedor = new javax.swing.JButton();
        btnEliminarProveedor = new javax.swing.JButton();
        btnNuevoProveedor = new javax.swing.JButton();
        btnEditarProveedor = new javax.swing.JButton();
        txtIdProveedor = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        txtCorreoProveedor = new javax.swing.JTextField();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        cbEstadoProveedor = new javax.swing.JComboBox<>();
        jLabel72 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        txtObservacionesProveedor = new javax.swing.JTextArea();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        txtCodigoPro = new javax.swing.JTextField();
        txtDesPro = new javax.swing.JTextField();
        txtCantPro = new javax.swing.JTextField();
        txtPrecioPro = new javax.swing.JTextField();
        cbxProveedorPro = new javax.swing.JComboBox<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        TableProducto = new javax.swing.JTable();
        btnGuardarpro = new javax.swing.JButton();
        btnEliminarPro = new javax.swing.JButton();
        btnEditarpro = new javax.swing.JButton();
        btnExcelPro = new javax.swing.JButton();
        btnNuevoPro = new javax.swing.JButton();
        txtIdpro = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        txtCostoProducto = new javax.swing.JTextField();
        txtFechaCaducidad = new com.toedter.calendar.JDateChooser();
        txtFechaElaboracion = new com.toedter.calendar.JDateChooser();
        jLabel73 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel74 = new javax.swing.JLabel();
        txtStockMinProducto = new javax.swing.JTextField();
        btnAlertaStock = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        TableVentas = new javax.swing.JTable();
        btnPdfVentas = new javax.swing.JButton();
        txtIdVenta = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jPanel7 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        txtTelefonoConfig = new javax.swing.JTextField();
        txtRucConfig = new javax.swing.JTextField();
        txtNombreConfig = new javax.swing.JTextField();
        txtDireccionConfig = new javax.swing.JTextField();
        txtRazonConfig = new javax.swing.JTextField();
        btnActualizarConfig = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        txtIdConfig = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jPanel8 = new javax.swing.JPanel();
        btnNuevaVenta2 = new javax.swing.JButton();
        btnClientes1 = new javax.swing.JButton();
        btnProveedor1 = new javax.swing.JButton();
        btnProductos1 = new javax.swing.JButton();
        btnVentas1 = new javax.swing.JButton();
        btnConfig1 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        TableAlertaStock = new javax.swing.JTable();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel47 = new javax.swing.JLabel();
        btnProductos2 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        TablePorVencer = new javax.swing.JTable();
        jScrollPane9 = new javax.swing.JScrollPane();
        TablePerdidas = new javax.swing.JTable();
        jLabel21 = new javax.swing.JLabel();
        lblTotalPerdidas = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jSeparator9 = new javax.swing.JSeparator();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        btnProductos3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("EcoTrack");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        LabelVendedor.setFont(new java.awt.Font("Segoe UI Black", 3, 18)); // NOI18N
        LabelVendedor.setText("Nombre");
        jPanel1.add(LabelVendedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 50, 190, 40));

        btnNuevaVenta.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnNuevaVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Nventa.png"))); // NOI18N
        btnNuevaVenta.setText("Nueva Venta");
        btnNuevaVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaVentaActionPerformed(evt);
            }
        });
        jPanel1.add(btnNuevaVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 20, 180, -1));

        btnProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/proveedor.png"))); // NOI18N
        btnProveedor.setText("Proveedor");
        btnProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProveedorActionPerformed(evt);
            }
        });
        jPanel1.add(btnProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 20, 150, 40));

        btnVentas.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/compras.png"))); // NOI18N
        btnVentas.setText("Ventas");
        btnVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentasActionPerformed(evt);
            }
        });
        jPanel1.add(btnVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 90, 140, 40));

        btnConfig.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config.png"))); // NOI18N
        btnConfig.setText("Configuración");
        btnConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigActionPerformed(evt);
            }
        });
        jPanel1.add(btnConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 90, 150, -1));

        jButton1.setBackground(new java.awt.Color(255, 102, 102));
        jButton1.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jButton1.setText("Cerrar Sesión");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 100, 140, 40));

        btnNuevaVenta1.setBackground(new java.awt.Color(0, 102, 0));
        btnNuevaVenta1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnNuevaVenta1.setForeground(new java.awt.Color(255, 255, 255));
        btnNuevaVenta1.setText("Página principal");
        btnNuevaVenta1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaVenta1ActionPerformed(evt);
            }
        });
        jPanel1.add(btnNuevaVenta1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 130, 30));

        btnClientes.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Clientes.png"))); // NOI18N
        btnClientes.setText("Clientes");
        btnClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClientesActionPerformed(evt);
            }
        });
        jPanel1.add(btnClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 20, 150, 40));

        jLabel59.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel59.setText("BIENVENID@!");
        jPanel1.add(jLabel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 20, 180, -1));

        jLabel60.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/login.png"))); // NOI18N
        jPanel1.add(jLabel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, -1, -1));

        btnProductos.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnProductos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/producto.png"))); // NOI18N
        btnProductos.setText("Productos");
        btnProductos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductosActionPerformed(evt);
            }
        });
        jPanel1.add(btnProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 90, 150, 40));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 950, 160));

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(204, 255, 204));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Código:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 50, -1, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Descripción:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 50, -1, -1));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Cantidad:");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 50, -1, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setText("Precio:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 50, -1, -1));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setText("Stock Disponible:");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 50, -1, -1));

        btnEliminarventa.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnEliminarventa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarventa.setText("Eliminar");
        btnEliminarventa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarventaActionPerformed(evt);
            }
        });
        jPanel2.add(btnEliminarventa, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 390, 110, -1));

        txtCodigoVenta.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtCodigoVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoVentaActionPerformed(evt);
            }
        });
        txtCodigoVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCodigoVentaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCodigoVentaKeyTyped(evt);
            }
        });
        jPanel2.add(txtCodigoVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 80, 90, -1));

        txtDescripcionVenta.setEditable(false);
        txtDescripcionVenta.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtDescripcionVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDescripcionVentaKeyTyped(evt);
            }
        });
        jPanel2.add(txtDescripcionVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 80, 170, -1));

        txtCantidadVenta.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtCantidadVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCantidadVentaActionPerformed(evt);
            }
        });
        txtCantidadVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCantidadVentaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantidadVentaKeyTyped(evt);
            }
        });
        jPanel2.add(txtCantidadVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 80, 90, -1));

        txtPrecioVenta.setEditable(false);
        txtPrecioVenta.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel2.add(txtPrecioVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 80, 90, -1));

        txtStockDisponible.setEditable(false);
        txtStockDisponible.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel2.add(txtStockDisponible, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 80, 80, -1));

        TablaVenta.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        TablaVenta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CÓDIGO", "DESCRIPCIÓN", "CANTIDAD", "PRECIO", "TOTAL"
            }
        ));
        jScrollPane1.setViewportView(TablaVenta);
        if (TablaVenta.getColumnModel().getColumnCount() > 0) {
            TablaVenta.getColumnModel().getColumn(0).setPreferredWidth(30);
            TablaVenta.getColumnModel().getColumn(1).setPreferredWidth(100);
            TablaVenta.getColumnModel().getColumn(2).setPreferredWidth(30);
            TablaVenta.getColumnModel().getColumn(3).setPreferredWidth(30);
            TablaVenta.getColumnModel().getColumn(4).setPreferredWidth(40);
        }

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 190, 710, 190));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("Nombre:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 140, -1, -1));

        txtNombreClienteventa.setEditable(false);
        txtNombreClienteventa.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel2.add(txtNombreClienteventa, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 160, 370, -1));

        txtRucVenta.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtRucVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtRucVentaKeyPressed(evt);
            }
        });
        jPanel2.add(txtRucVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 160, 180, -1));

        btnGenerarVenta.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnGenerarVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/print.png"))); // NOI18N
        btnGenerarVenta.setText("Generar Venta");
        btnGenerarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarVentaActionPerformed(evt);
            }
        });
        jPanel2.add(btnGenerarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 390, -1, 30));

        LabelTotal.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        LabelTotal.setText("-----");
        jPanel2.add(LabelTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 390, 50, 30));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png"))); // NOI18N
        jLabel11.setText("Total a pagar:");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 380, -1, -1));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel10.setText("CED/RUC:");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 140, -1, -1));
        jPanel2.add(txtTelefonoCV, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 10, 10, -1));
        jPanel2.add(txtDireccionCV, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 10, 10, -1));
        jPanel2.add(txtRazonCV, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 10, 10, -1));
        jPanel2.add(txtIdPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 10, 10, -1));

        btnGraficar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnGraficar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/torta.png"))); // NOI18N
        btnGraficar.setText("Generar reporte");
        btnGraficar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGraficarActionPerformed(evt);
            }
        });
        jPanel2.add(btnGraficar, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 200, 40));
        jPanel2.add(Midate, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, 180, -1));

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel35.setText("ver reporte:");
        jPanel2.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, -1, -1));

        jLabel34.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel34.setText("Ventas");
        jPanel2.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 110, -1));

        jLabel36.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel36.setText("Datos del cliente:");
        jPanel2.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 110, -1, -1));

        jLabel37.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel37.setText("Datos del producto:");
        jPanel2.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 10, -1, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Carrito-de-compras.png"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 40, 40));
        jPanel2.add(txtVendedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 10, 10, -1));

        jLabel62.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel62.setText("la venta.");
        jPanel2.add(jLabel62, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, -1));

        jLabel63.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel63.setText("Seleccionar fecha para");
        jPanel2.add(jLabel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, -1, -1));

        jLabel64.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel64.setText("* Todos los campos son ");
        jPanel2.add(jLabel64, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, -1));

        jLabel65.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel65.setText("en el apartado \"Productos\".");
        jPanel2.add(jLabel65, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, -1, -1));

        jLabel66.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel66.setText("obligatorios para registrar");
        jPanel2.add(jLabel66, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jLabel67.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel67.setText("En caso de no conocer el ");
        jPanel2.add(jLabel67, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, -1, -1));

        jLabel68.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel68.setText("código de producto consulte");
        jPanel2.add(jLabel68, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        jPanel2.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 190, 10));

        txtBuscarProducto.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel2.add(txtBuscarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 12, 250, 30));

        btnBuscarProducto.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnBuscarProducto.setText("Buscar\n");
        btnBuscarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarProductoActionPerformed(evt);
            }
        });
        jPanel2.add(btnBuscarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 20, -1, -1));

        jTabbedPane1.addTab("Ventas", jPanel2);

        jPanel3.setBackground(new java.awt.Color(204, 255, 204));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel12.setText("CED/RUC:");
        jPanel3.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 10, -1, -1));

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel14.setText("Teléfono:");
        jPanel3.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 10, -1, -1));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel15.setText("Dirección:");
        jPanel3.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 60, -1, -1));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel16.setText("Razón social:");
        jPanel3.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 60, -1, -1));

        txtRazonCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel3.add(txtRazonCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 80, 380, -1));

        txtDniCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtDniCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDniClienteKeyTyped(evt);
            }
        });
        jPanel3.add(txtDniCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 30, 170, -1));

        txtTelefonoCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtTelefonoCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtTelefonoClienteKeyTyped(evt);
            }
        });
        jPanel3.add(txtTelefonoCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 30, 200, -1));

        txtDireccionCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtDireccionCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDireccionClienteActionPerformed(evt);
            }
        });
        txtDireccionCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDireccionClienteKeyTyped(evt);
            }
        });
        jPanel3.add(txtDireccionCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 80, 310, -1));

        TableCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        TableCliente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "CED/RUC", "Nombre", "Teléfono", "Dirección", "Razón social"
            }
        ));
        TableCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableClienteMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(TableCliente);
        if (TableCliente.getColumnModel().getColumnCount() > 0) {
            TableCliente.getColumnModel().getColumn(1).setPreferredWidth(50);
            TableCliente.getColumnModel().getColumn(2).setPreferredWidth(100);
            TableCliente.getColumnModel().getColumn(3).setPreferredWidth(50);
            TableCliente.getColumnModel().getColumn(4).setPreferredWidth(80);
            TableCliente.getColumnModel().getColumn(5).setPreferredWidth(80);
        }

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 930, 250));

        btnEditarCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnEditarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Actualizar (2).png"))); // NOI18N
        btnEditarCliente.setText("Actualizar");
        btnEditarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarClienteActionPerformed(evt);
            }
        });
        jPanel3.add(btnEditarCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 380, -1, -1));

        btnGuardarCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnGuardarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/GuardarTodo.png"))); // NOI18N
        btnGuardarCliente.setText("Guardar");
        btnGuardarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarClienteActionPerformed(evt);
            }
        });
        jPanel3.add(btnGuardarCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, -1, -1));

        btnEliminarCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnEliminarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarCliente.setText("Eliminar");
        btnEliminarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarClienteActionPerformed(evt);
            }
        });
        jPanel3.add(btnEliminarCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 380, -1, 30));

        btnNuevoCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnNuevoCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/nuevo.png"))); // NOI18N
        btnNuevoCliente.setText("Ingresar");
        btnNuevoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoClienteActionPerformed(evt);
            }
        });
        jPanel3.add(btnNuevoCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 380, -1, -1));
        jPanel3.add(txtIdCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 390, 20, -1));

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Clientes.png"))); // NOI18N
        jPanel3.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, 40, 40));

        jLabel40.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel40.setText("Clientes");
        jPanel3.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 130, -1));

        jLabel44.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel44.setText("Nombre:");
        jPanel3.add(jLabel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 10, -1, -1));

        txtNombreCliente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtNombreCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNombreClienteKeyTyped(evt);
            }
        });
        jPanel3.add(txtNombreCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 30, 240, -1));

        jSeparator4.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 190, 10));

        jTabbedPane1.addTab("Clientes", jPanel3);

        jPanel4.setBackground(new java.awt.Color(204, 255, 204));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel17.setText("Razón social:");
        jPanel4.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 70, -1, -1));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel18.setText("RUC:");
        jPanel4.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 20, -1, -1));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel19.setText("Nombre:");
        jPanel4.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 20, -1, -1));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel20.setText("Teléfono:");
        jPanel4.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 20, -1, -1));

        txtRazonProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtRazonProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRazonProveedorActionPerformed(evt);
            }
        });
        jPanel4.add(txtRazonProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 90, 230, -1));

        txtRucProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtRucProveedor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtRucProveedorKeyTyped(evt);
            }
        });
        jPanel4.add(txtRucProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 40, 160, -1));

        txtNombreproveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtNombreproveedor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNombreproveedorKeyTyped(evt);
            }
        });
        jPanel4.add(txtNombreproveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, 230, -1));

        txtTelefonoProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtTelefonoProveedor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtTelefonoProveedorKeyTyped(evt);
            }
        });
        jPanel4.add(txtTelefonoProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 40, 190, -1));

        txtDireccionProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtDireccionProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDireccionProveedorActionPerformed(evt);
            }
        });
        txtDireccionProveedor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDireccionProveedorKeyTyped(evt);
            }
        });
        jPanel4.add(txtDireccionProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 90, 190, -1));

        TableProveedor.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "RUC", "Nombre", "Teléfono", "Dirección", "Razón social", "Email", "Estado", "Observaciones"
            }
        ));
        TableProveedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableProveedorMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(TableProveedor);
        if (TableProveedor.getColumnModel().getColumnCount() > 0) {
            TableProveedor.getColumnModel().getColumn(0).setPreferredWidth(20);
            TableProveedor.getColumnModel().getColumn(1).setPreferredWidth(40);
            TableProveedor.getColumnModel().getColumn(2).setPreferredWidth(100);
            TableProveedor.getColumnModel().getColumn(3).setPreferredWidth(50);
            TableProveedor.getColumnModel().getColumn(4).setPreferredWidth(80);
            TableProveedor.getColumnModel().getColumn(5).setPreferredWidth(70);
        }

        jPanel4.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 930, 190));

        btnguardarProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnguardarProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/GuardarTodo.png"))); // NOI18N
        btnguardarProveedor.setText("Guardar");
        btnguardarProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnguardarProveedorActionPerformed(evt);
            }
        });
        jPanel4.add(btnguardarProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 380, -1, -1));

        btnEliminarProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnEliminarProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarProveedor.setText("Eliminar");
        btnEliminarProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarProveedorActionPerformed(evt);
            }
        });
        jPanel4.add(btnEliminarProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 380, -1, 30));

        btnNuevoProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnNuevoProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/nuevo.png"))); // NOI18N
        btnNuevoProveedor.setText("Ingresar");
        btnNuevoProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoProveedorActionPerformed(evt);
            }
        });
        jPanel4.add(btnNuevoProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 380, -1, -1));

        btnEditarProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnEditarProveedor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Actualizar (2).png"))); // NOI18N
        btnEditarProveedor.setText("Actualizar");
        btnEditarProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarProveedorActionPerformed(evt);
            }
        });
        jPanel4.add(btnEditarProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 380, -1, -1));
        jPanel4.add(txtIdProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 10, -1));

        jLabel42.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel42.setText("Proveedores");
        jPanel4.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 210, -1));

        jLabel43.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/proveedor.png"))); // NOI18N
        jPanel4.add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 40, 40));

        jLabel69.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel69.setText("Dirección:");
        jPanel4.add(jLabel69, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 70, -1, -1));

        txtCorreoProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel4.add(txtCorreoProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 310, -1));

        jLabel70.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel70.setText("Estado del proveedor:");
        jPanel4.add(jLabel70, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 70, -1, -1));

        jLabel71.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel71.setText("Observaciones:");
        jPanel4.add(jLabel71, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 120, -1, -1));

        cbEstadoProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cbEstadoProveedor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Activo\t", "Inactivo" }));
        jPanel4.add(cbEstadoProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 90, 130, -1));

        jLabel72.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel72.setText("Correo electrónico:");
        jPanel4.add(jLabel72, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, -1));

        txtObservacionesProveedor.setColumns(20);
        txtObservacionesProveedor.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtObservacionesProveedor.setRows(5);
        jScrollPane6.setViewportView(txtObservacionesProveedor);

        jPanel4.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 140, 310, 30));

        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));
        jPanel4.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 190, 10));

        jTabbedPane1.addTab("Proveedor", jPanel4);

        jPanel5.setBackground(new java.awt.Color(204, 255, 204));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel22.setText("Código:");
        jPanel5.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 10, -1, -1));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel23.setText("Descripción:");
        jPanel5.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 10, -1, -1));

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel24.setText("Fecha de caducidad:");
        jPanel5.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 60, -1, -1));

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel25.setText("Pvp:");
        jPanel5.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 10, 50, -1));

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel26.setText("Proveedor:");
        jPanel5.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 60, -1, -1));

        txtCodigoPro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtCodigoPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoProActionPerformed(evt);
            }
        });
        jPanel5.add(txtCodigoPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 30, 110, -1));

        txtDesPro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel5.add(txtDesPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 30, 190, -1));

        txtCantPro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtCantPro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantProKeyTyped(evt);
            }
        });
        jPanel5.add(txtCantPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 30, 80, -1));

        txtPrecioPro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtPrecioPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrecioProActionPerformed(evt);
            }
        });
        txtPrecioPro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPrecioProKeyTyped(evt);
            }
        });
        jPanel5.add(txtPrecioPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 30, 60, -1));

        cbxProveedorPro.setEditable(true);
        cbxProveedorPro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel5.add(cbxProveedorPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 80, 150, -1));

        TableProducto.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        TableProducto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Código", "Descripción", "Proveedor", "Stock", "Precio", "Costo", "Fecha de elaboracion", "Fecha de caducidad", "Stock minimo"
            }
        ));
        TableProducto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableProductoMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(TableProducto);
        if (TableProducto.getColumnModel().getColumnCount() > 0) {
            TableProducto.getColumnModel().getColumn(0).setPreferredWidth(20);
            TableProducto.getColumnModel().getColumn(1).setPreferredWidth(50);
            TableProducto.getColumnModel().getColumn(2).setPreferredWidth(100);
            TableProducto.getColumnModel().getColumn(3).setPreferredWidth(60);
            TableProducto.getColumnModel().getColumn(4).setPreferredWidth(40);
            TableProducto.getColumnModel().getColumn(5).setPreferredWidth(50);
        }

        jPanel5.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 920, 230));

        btnGuardarpro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnGuardarpro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/GuardarTodo.png"))); // NOI18N
        btnGuardarpro.setText("Guardar");
        btnGuardarpro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarproActionPerformed(evt);
            }
        });
        jPanel5.add(btnGuardarpro, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 350, -1, -1));

        btnEliminarPro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnEliminarPro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarPro.setText("Eliminar");
        btnEliminarPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarProActionPerformed(evt);
            }
        });
        jPanel5.add(btnEliminarPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 350, -1, -1));

        btnEditarpro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnEditarpro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Actualizar (2).png"))); // NOI18N
        btnEditarpro.setText("Actualizar");
        btnEditarpro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarproActionPerformed(evt);
            }
        });
        jPanel5.add(btnEditarpro, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 350, -1, -1));

        btnExcelPro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnExcelPro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/excel.png"))); // NOI18N
        btnExcelPro.setText("Crear reporte");
        btnExcelPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcelProActionPerformed(evt);
            }
        });
        jPanel5.add(btnExcelPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 350, -1, -1));

        btnNuevoPro.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnNuevoPro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/nuevo.png"))); // NOI18N
        btnNuevoPro.setText("Ingresar");
        btnNuevoPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoProActionPerformed(evt);
            }
        });
        jPanel5.add(btnNuevoPro, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 350, -1, -1));
        jPanel5.add(txtIdpro, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 20, -1));

        jLabel55.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel55.setText("Productos");
        jPanel5.add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 170, 50));

        jLabel56.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel56.setText("Stock minimo:");
        jPanel5.add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 60, 90, -1));

        jLabel57.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel57.setText("Costo: ");
        jPanel5.add(jLabel57, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 10, -1, -1));

        jLabel58.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel58.setText("Fecha de elaboración:");
        jPanel5.add(jLabel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 60, -1, -1));

        txtCostoProducto.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel5.add(txtCostoProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 30, 120, -1));
        jPanel5.add(txtFechaCaducidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 80, 130, -1));
        jPanel5.add(txtFechaElaboracion, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 80, 160, -1));

        jLabel73.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/producto.png"))); // NOI18N
        jPanel5.add(jLabel73, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 40, 40, 40));

        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jPanel5.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 190, 10));

        jLabel74.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel74.setText("Cantidad:");
        jPanel5.add(jLabel74, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 10, -1, -1));

        txtStockMinProducto.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel5.add(txtStockMinProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 80, 110, -1));

        btnAlertaStock.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnAlertaStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/257195 (1).png"))); // NOI18N
        btnAlertaStock.setText("Productos con bajo stock!");
        btnAlertaStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlertaStockActionPerformed(evt);
            }
        });
        jPanel5.add(btnAlertaStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 350, -1, 30));

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/257195 (1).png"))); // NOI18N
        jButton2.setText("Productos vencidos o por vencer");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 390, 290, -1));

        jTabbedPane1.addTab("Productos", jPanel5);

        jPanel6.setBackground(new java.awt.Color(204, 255, 204));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TableVentas.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        TableVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Cliente", "Vendedor", "Total"
            }
        ));
        TableVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableVentasMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(TableVentas);
        if (TableVentas.getColumnModel().getColumnCount() > 0) {
            TableVentas.getColumnModel().getColumn(0).setPreferredWidth(20);
            TableVentas.getColumnModel().getColumn(1).setPreferredWidth(60);
            TableVentas.getColumnModel().getColumn(2).setPreferredWidth(60);
            TableVentas.getColumnModel().getColumn(3).setPreferredWidth(60);
        }

        jPanel6.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 930, 240));

        btnPdfVentas.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnPdfVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pdf.png"))); // NOI18N
        btnPdfVentas.setText("Generar PDF");
        btnPdfVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPdfVentasActionPerformed(evt);
            }
        });
        jPanel6.add(btnPdfVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 370, 210, 40));
        jPanel6.add(txtIdVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 10, -1));

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel13.setText("Seleccione la venta a ser reimprimida:");
        jPanel6.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));

        jLabel45.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel45.setText("Reimpresión de facturas de ventas");
        jPanel6.add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 560, -1));

        jLabel46.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/producto.png"))); // NOI18N
        jPanel6.add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 40, 40));

        jLabel49.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pdf.png"))); // NOI18N
        jPanel6.add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 50, 40, 40));

        jSeparator5.setForeground(new java.awt.Color(0, 0, 0));
        jPanel6.add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 540, 10));

        jTabbedPane1.addTab("Ventas", jPanel6);

        jPanel7.setBackground(new java.awt.Color(204, 255, 204));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel27.setText("Teléfono:");
        jPanel7.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 60, -1, -1));

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel28.setText("CED/RUC:");
        jPanel7.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 60, -1, -1));

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel29.setText("Nombre:");
        jPanel7.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 60, -1, -1));

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel30.setText("Razón social:");
        jPanel7.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 110, -1, -1));

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel31.setText("Dirección:");
        jPanel7.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 110, -1, -1));

        txtTelefonoConfig.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtTelefonoConfig.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtTelefonoConfigKeyTyped(evt);
            }
        });
        jPanel7.add(txtTelefonoConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 80, 200, -1));

        txtRucConfig.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtRucConfig.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtRucConfigKeyTyped(evt);
            }
        });
        jPanel7.add(txtRucConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 80, 190, -1));

        txtNombreConfig.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        txtNombreConfig.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtNombreConfigKeyTyped(evt);
            }
        });
        jPanel7.add(txtNombreConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 80, 230, -1));

        txtDireccionConfig.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel7.add(txtDireccionConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 130, 270, -1));

        txtRazonConfig.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPanel7.add(txtRazonConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 130, 230, -1));

        btnActualizarConfig.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        btnActualizarConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Actualizar (2).png"))); // NOI18N
        btnActualizarConfig.setText("Actualizar");
        btnActualizarConfig.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnActualizarConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarConfigActionPerformed(evt);
            }
        });
        jPanel7.add(btnActualizarConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 120, 140, 30));

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel32.setText("Actualizar datos");
        jPanel7.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, -1, -1));
        jPanel7.add(txtIdConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 20, 10, -1));

        jLabel50.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel50.setText("de la empresa");
        jPanel7.add(jLabel50, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        jLabel52.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config.png"))); // NOI18N
        jPanel7.add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 40, 50));

        jLabel41.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel41.setText("Configuración");
        jPanel7.add(jLabel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, 230, -1));

        jSeparator6.setForeground(new java.awt.Color(0, 0, 0));
        jPanel7.add(jSeparator6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 920, 10));

        jSeparator7.setForeground(new java.awt.Color(0, 0, 0));
        jPanel7.add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 270, 10));

        jTabbedPane1.addTab("Configuración", jPanel7);

        jPanel8.setBackground(new java.awt.Color(204, 255, 204));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnNuevaVenta2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnNuevaVenta2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Nventa.png"))); // NOI18N
        btnNuevaVenta2.setText("Nueva Venta");
        btnNuevaVenta2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaVenta2ActionPerformed(evt);
            }
        });
        jPanel8.add(btnNuevaVenta2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 210, 280, 90));

        btnClientes1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnClientes1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Clientes.png"))); // NOI18N
        btnClientes1.setText("Clientes");
        btnClientes1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClientes1ActionPerformed(evt);
            }
        });
        jPanel8.add(btnClientes1, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 210, 240, 90));

        btnProveedor1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnProveedor1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/proveedor.png"))); // NOI18N
        btnProveedor1.setText("Proveedor");
        btnProveedor1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProveedor1ActionPerformed(evt);
            }
        });
        jPanel8.add(btnProveedor1, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 220, 200, 80));

        btnProductos1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnProductos1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/producto.png"))); // NOI18N
        btnProductos1.setText("Productos");
        btnProductos1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductos1ActionPerformed(evt);
            }
        });
        jPanel8.add(btnProductos1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 320, 280, 90));

        btnVentas1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnVentas1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/compras.png"))); // NOI18N
        btnVentas1.setText("Ventas");
        btnVentas1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentas1ActionPerformed(evt);
            }
        });
        jPanel8.add(btnVentas1, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 320, 230, 90));

        btnConfig1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnConfig1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config.png"))); // NOI18N
        btnConfig1.setText("Configuración");
        btnConfig1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfig1ActionPerformed(evt);
            }
        });
        jPanel8.add(btnConfig1, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 320, 200, 80));

        jLabel9.setFont(new java.awt.Font("Tahoma", 3, 24)); // NOI18N
        jLabel9.setText("comodidad y control en tus manos");
        jPanel8.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 160, 440, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/login.png"))); // NOI18N
        jPanel8.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 50, -1, -1));

        jLabel61.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel61.setText("Sistema de Gestión de Ventas e Inventario");
        jPanel8.add(jLabel61, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 20, 530, -1));

        jTabbedPane1.addTab("Pag. Principal", jPanel8);

        jPanel9.setBackground(new java.awt.Color(204, 255, 204));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TableAlertaStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Producto", "Stock", "Stock minimo", "Proveedor"
            }
        ));
        jScrollPane7.setViewportView(TableAlertaStock);

        jPanel9.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 880, 300));

        jSeparator8.setForeground(new java.awt.Color(0, 0, 0));
        jPanel9.add(jSeparator8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 410, 10));

        jLabel47.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel47.setText("Productos con stock bajo!");
        jPanel9.add(jLabel47, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 420, -1));

        btnProductos2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnProductos2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/producto.png"))); // NOI18N
        btnProductos2.setText("Registrar productos");
        btnProductos2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductos2ActionPerformed(evt);
            }
        });
        jPanel9.add(btnProductos2, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 380, 190, 40));

        jTabbedPane1.addTab("Productos por agotarse", jPanel9);

        jPanel10.setBackground(new java.awt.Color(204, 255, 204));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TablePorVencer.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Código", "Nombre", "Stock", "Fecha Caducidad"
            }
        ));
        jScrollPane8.setViewportView(TablePorVencer);

        jPanel10.add(jScrollPane8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 920, 100));

        TablePerdidas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Producto", "Cantidad", "Costo unitario", "Costo total", "Motivo", "Fecha"
            }
        ));
        jScrollPane9.setViewportView(TablePerdidas);

        jPanel10.add(jScrollPane9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 900, 100));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png"))); // NOI18N
        jLabel21.setText("Total de perdidas:");
        jPanel10.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 380, -1, -1));

        lblTotalPerdidas.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblTotalPerdidas.setText("-----");
        jPanel10.add(lblTotalPerdidas, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 376, 60, 30));

        jLabel75.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel75.setText("Productos vencidos");
        jPanel10.add(jLabel75, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 310, 50));

        jLabel76.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/producto.png"))); // NOI18N
        jPanel10.add(jLabel76, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 40, 40, 40));

        jSeparator9.setForeground(new java.awt.Color(0, 0, 0));
        jPanel10.add(jSeparator9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 310, 10));

        jLabel77.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel77.setText("Productos vencidos :(");
        jPanel10.add(jLabel77, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 290, -1));

        jLabel78.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel78.setText("Productos por vencer :0");
        jPanel10.add(jLabel78, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 530, -1));

        btnProductos3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnProductos3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/producto.png"))); // NOI18N
        btnProductos3.setText("Revisar stock general de productos");
        btnProductos3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductos3ActionPerformed(evt);
            }
        });
        jPanel10.add(btnProductos3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 380, -1, 40));

        jTabbedPane1.addTab("Vencidos", jPanel10);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 160, 950, 460));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigActionPerformed
        jTabbedPane1.setSelectedIndex(5);
    }//GEN-LAST:event_btnConfigActionPerformed

    private void txtCodigoVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoVentaActionPerformed

    private void txtDireccionClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDireccionClienteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDireccionClienteActionPerformed

    private void txtDireccionProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDireccionProveedorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDireccionProveedorActionPerformed

    private void txtPrecioProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrecioProActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrecioProActionPerformed

    private void btnGuardarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarClienteActionPerformed
    

    if (txtDniCliente.getText().isEmpty() ||
        txtNombreCliente.getText().isEmpty() ||
        txtTelefonoCliente.getText().isEmpty() ||
        txtDireccionCliente.getText().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Los campos están vacíos");
        return;
    }

    if (txtDniCliente.getText().length() != 10) {
        JOptionPane.showMessageDialog(null, "La cédula debe tener 10 dígitos");
        return;
    }

    if (!validarCedula(txtDniCliente.getText())) {
        JOptionPane.showMessageDialog(null, "Cédula incorrecta");
        return;
    }

    Clientes c = new Clientes();
    c.setDni(Integer.parseInt(txtDniCliente.getText()));
    c.setNombre(txtNombreCliente.getText());
    c.setTelefono(Integer.parseInt(txtTelefonoCliente.getText()));
    c.setDireccion(txtDireccionCliente.getText());
    c.setRazon(txtRazonCliente.getText());
    c.setFecha(new Date());

    try {
        clientesJpa.create(c);
        JOptionPane.showMessageDialog(null, "Cliente registrado");
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
    }

    LimpiarTable(TableCliente);
    LimpiarCliente();
    ListarCliente();



    }//GEN-LAST:event_btnGuardarClienteActionPerformed
    private void LimpiarCliente() {
        txtIdCliente.setText("");
        txtDniCliente.setText("");
        txtNombreCliente.setText("");
        txtTelefonoCliente.setText("");
        txtDireccionCliente.setText("");
        txtRazonCliente.setText("");

    }
   private void LimpiarProveedor() {
    txtIdProveedor.setText("");
    txtRucProveedor.setText("");
    txtNombreproveedor.setText("");
    txtTelefonoProveedor.setText("");
    txtDireccionProveedor.setText("");
    txtRazonProveedor.setText("");
    txtCorreoProveedor.setText("");
    cbEstadoProveedor.setSelectedIndex(0); 
    txtObservacionesProveedor.setText("");
}

     
 private void LimpiarProductos() {
    txtIdPro.setText("");
    txtCodigoPro.setText("");
    txtDesPro.setText("");
    cbxProveedorPro.setSelectedIndex(0);
    txtCantPro.setText("");
    txtPrecioPro.setText("");
    txtCostoProducto.setText("");
    txtStockMinProducto.setText(""); 

    txtFechaElaboracion.setDate(null);
    txtFechaCaducidad.setDate(null);
}


    private void btnClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClientesActionPerformed
        LimpiarTable(TableCliente);
        ListarCliente();
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_btnClientesActionPerformed

    private void TableClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableClienteMouseClicked
        int fila = TableCliente.rowAtPoint(evt.getPoint());
        txtIdCliente.setText(TableCliente.getValueAt(fila, 0).toString());
        txtDniCliente.setText(TableCliente.getValueAt(fila, 1).toString());
        txtNombreCliente.setText(TableCliente.getValueAt(fila, 2).toString());
        txtTelefonoCliente.setText(TableCliente.getValueAt(fila, 3).toString());
        txtDireccionCliente.setText(TableCliente.getValueAt(fila, 4).toString());
        txtRazonCliente.setText(TableCliente.getValueAt(fila, 5).toString());
    }//GEN-LAST:event_TableClienteMouseClicked

    private void btnEliminarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarClienteActionPerformed

    if (txtIdCliente.getText().isEmpty()) {
        return;
    }

    int pregunta = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar?");
    if (pregunta != 0) {
        return;
    }

    try {
        clientesJpa.destroy(Integer.parseInt(txtIdCliente.getText()));
        JOptionPane.showMessageDialog(null, "Cliente eliminado");
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
    }

    LimpiarTable(TableCliente);
    LimpiarCliente();
    ListarCliente();


    }//GEN-LAST:event_btnEliminarClienteActionPerformed

    private void btnEditarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarClienteActionPerformed

    if (txtIdCliente.getText().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Seleccione una fila");
        return;
    }

    Clientes c = clientesJpa.findClientes(Integer.parseInt(txtIdCliente.getText()));
    if (c == null) {
        JOptionPane.showMessageDialog(null, "Cliente no encontrado");
        return;
    }

    c.setDni(Integer.parseInt(txtDniCliente.getText()));
    c.setNombre(txtNombreCliente.getText());
    c.setTelefono(Integer.parseInt(txtTelefonoCliente.getText()));
    c.setDireccion(txtDireccionCliente.getText());
    c.setRazon(txtRazonCliente.getText());

    try {
        clientesJpa.edit(c);
        JOptionPane.showMessageDialog(null, "Cliente modificado");
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
    }

    LimpiarTable(TableCliente);
    LimpiarCliente();
    ListarCliente();

    }//GEN-LAST:event_btnEditarClienteActionPerformed

    private void btnNuevoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoClienteActionPerformed
        LimpiarCliente();
    }//GEN-LAST:event_btnNuevoClienteActionPerformed

    private void btnguardarProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnguardarProveedorActionPerformed
    if (txtRucProveedor.getText().isEmpty() ||
        txtNombreproveedor.getText().isEmpty() ||
        txtTelefonoProveedor.getText().isEmpty() ||
        txtDireccionProveedor.getText().isEmpty() ||
        txtRazonProveedor.getText().isEmpty() ||
        txtCorreoProveedor.getText().isEmpty() ||
        cbEstadoProveedor.getSelectedItem() == null) {

        JOptionPane.showMessageDialog(null, "Todos los campos obligatorios deben ser llenados");
        return;
    }

    try {
        Proveedor p = new Proveedor();

        p.setRuc(Integer.parseInt(txtRucProveedor.getText()));
        p.setNombre(txtNombreproveedor.getText());
        p.setTelefono(Integer.parseInt(txtTelefonoProveedor.getText()));
        p.setDireccion(txtDireccionProveedor.getText());
        p.setRazon(txtRazonProveedor.getText());

        // NUEVOS CAMPOS
        p.setCorreo(txtCorreoProveedor.getText());
        p.setEstado(cbEstadoProveedor.getSelectedItem().toString());
        p.setObservaciones(txtObservacionesProveedor.getText());

        p.setFecha(new Date()); 

        proveedorJpa.create(p);

        JOptionPane.showMessageDialog(null, "Proveedor registrado correctamente");

        LimpiarTable(TableProveedor);
        ListarProveedor();
        LimpiarProveedor();

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Error al registrar proveedor: " + ex.getMessage());
    }
    }//GEN-LAST:event_btnguardarProveedorActionPerformed

    private void btnProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProveedorActionPerformed
        LimpiarTable(TableProveedor);
        ListarProveedor();
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_btnProveedorActionPerformed

    private void TableProveedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableProveedorMouseClicked
    int fila = TableProveedor.rowAtPoint(evt.getPoint());
    
    txtIdProveedor.setText(TableProveedor.getValueAt(fila, 0).toString());
    txtRucProveedor.setText(TableProveedor.getValueAt(fila, 1).toString());
    txtNombreproveedor.setText(TableProveedor.getValueAt(fila, 2).toString());
    txtTelefonoProveedor.setText(TableProveedor.getValueAt(fila, 3).toString());
    txtDireccionProveedor.setText(TableProveedor.getValueAt(fila, 4).toString());
    txtRazonProveedor.setText(TableProveedor.getValueAt(fila, 5).toString());

    // Nuevos campos
    txtCorreoProveedor.setText(TableProveedor.getValueAt(fila, 6) != null ? 
                               TableProveedor.getValueAt(fila, 6).toString() : "");

    cbEstadoProveedor.setSelectedItem(TableProveedor.getValueAt(fila, 7) != null ? 
                                      TableProveedor.getValueAt(fila, 7).toString() : "Activo");

    txtObservacionesProveedor.setText(TableProveedor.getValueAt(fila, 8) != null ?
                                      TableProveedor.getValueAt(fila, 8).toString() : "");

    }//GEN-LAST:event_TableProveedorMouseClicked

    private void btnEliminarProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarProveedorActionPerformed
    if (txtIdProveedor.getText().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Seleccione una fila");
        return;
    }

    int pregunta = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar?");

    if (pregunta == 0) {
        try {
            int id = Integer.parseInt(txtIdProveedor.getText());
            proveedorJpa.destroy(id);

            LimpiarTable(TableProveedor);
            ListarProveedor();
            LimpiarProveedor();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error al eliminar proveedor: " + ex.getMessage());
        }
    }
    }//GEN-LAST:event_btnEliminarProveedorActionPerformed

    private void btnEditarProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarProveedorActionPerformed
    if (txtIdProveedor.getText().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Seleccione un proveedor de la tabla");
        return;
    }

    try {
        Proveedor p = proveedorJpa.findProveedor(Integer.parseInt(txtIdProveedor.getText()));

        if (p == null) {
            JOptionPane.showMessageDialog(null, "El proveedor no existe");
            return;
        }

        p.setRuc(Integer.parseInt(txtRucProveedor.getText()));
        p.setNombre(txtNombreproveedor.getText());
        p.setTelefono(Integer.parseInt(txtTelefonoProveedor.getText()));
        p.setDireccion(txtDireccionProveedor.getText());
        p.setRazon(txtRazonProveedor.getText());

        // NUEVOS CAMPOS
        p.setCorreo(txtCorreoProveedor.getText());
        p.setEstado(cbEstadoProveedor.getSelectedItem().toString());
        p.setObservaciones(txtObservacionesProveedor.getText());

        proveedorJpa.edit(p);

        JOptionPane.showMessageDialog(null, "Proveedor modificado correctamente");

        LimpiarTable(TableProveedor);
        ListarProveedor();
        LimpiarProveedor();

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Error al modificar proveedor: " + ex.getMessage());
    }
    }//GEN-LAST:event_btnEditarProveedorActionPerformed

    private void btnNuevoProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoProveedorActionPerformed
        LimpiarProveedor();
    }//GEN-LAST:event_btnNuevoProveedorActionPerformed

    private void btnGuardarproActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarproActionPerformed
    if (txtCodigoPro.getText().isEmpty()
        || txtDesPro.getText().isEmpty()
        || txtCantPro.getText().isEmpty()
        || txtPrecioPro.getText().isEmpty()
        || txtCostoProducto.getText().isEmpty()
        || txtStockMinProducto.getText().isEmpty()
        || txtFechaElaboracion.getDate() == null
        || txtFechaCaducidad.getDate() == null) {

        JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios");
        return;
    }

    try {
        Productos p = new Productos();

        p.setCodigo(txtCodigoPro.getText());
        p.setNombre(txtDesPro.getText());
        p.setProveedor(cbxProveedorPro.getSelectedItem().toString());
        p.setStock(Integer.parseInt(txtCantPro.getText()));
        p.setStockMin(Integer.parseInt(txtStockMinProducto.getText()));

        p.setPrecio(new BigDecimal(txtPrecioPro.getText()));
        p.setCosto(new BigDecimal(txtCostoProducto.getText()));

        p.setFecha(new Date());
        p.setFechaElaboracion(txtFechaElaboracion.getDate());
        p.setFechaCaducidad(txtFechaCaducidad.getDate());

        productosJpa.create(p);

        JOptionPane.showMessageDialog(null, "Producto registrado correctamente");

        LimpiarTable(TableProducto);
        ListarProductos();
        LimpiarProductos();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error al registrar: " + e.getMessage());
    }
    }//GEN-LAST:event_btnGuardarproActionPerformed

    private void btnProductosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductosActionPerformed
        LimpiarTable(TableProducto);
        ListarProductos();
        jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_btnProductosActionPerformed

    private void TableProductoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableProductoMouseClicked
    int fila = TableProducto.rowAtPoint(evt.getPoint());

    txtIdPro.setText(TableProducto.getValueAt(fila, 0).toString());
    txtCodigoPro.setText(TableProducto.getValueAt(fila, 1).toString());
    txtDesPro.setText(TableProducto.getValueAt(fila, 2).toString());
    cbxProveedorPro.setSelectedItem(TableProducto.getValueAt(fila, 3).toString());
    txtCantPro.setText(TableProducto.getValueAt(fila, 4).toString());
    txtPrecioPro.setText(TableProducto.getValueAt(fila, 5).toString());
    txtCostoProducto.setText(TableProducto.getValueAt(fila, 6).toString());

    txtFechaElaboracion.setDate((Date) TableProducto.getValueAt(fila, 7));
    txtFechaCaducidad.setDate((Date) TableProducto.getValueAt(fila, 8));

    txtStockMinProducto.setText(TableProducto.getValueAt(fila, 9).toString());
    }//GEN-LAST:event_TableProductoMouseClicked

    private void btnEliminarProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarProActionPerformed
    if (txtIdPro.getText().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Seleccione un producto");
        return;
    }

    int pregunta = JOptionPane.showConfirmDialog(null, "¿Desea eliminar este producto?");
    if (pregunta != 0) {
        return;
    }

    try {
        int id = Integer.parseInt(txtIdPro.getText());
        productosJpa.destroy(id);

        JOptionPane.showMessageDialog(null, "Producto eliminado correctamente");

        LimpiarTable(TableProducto);
        ListarProductos();
        LimpiarProductos();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
    }
    }//GEN-LAST:event_btnEliminarProActionPerformed

    private void btnEditarproActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarproActionPerformed

    if (txtIdPro.getText().isEmpty()) {
        JOptionPane.showMessageDialog(null, "Seleccione un producto");
        return;
    }

    try {
        Productos p = productosJpa.findProductos(Integer.parseInt(txtIdPro.getText()));

        p.setCodigo(txtCodigoPro.getText());
        p.setNombre(txtDesPro.getText());
        p.setProveedor(cbxProveedorPro.getSelectedItem().toString());

        p.setStock(Integer.parseInt(txtCantPro.getText()));
        p.setStockMin(Integer.parseInt(txtStockMinProducto.getText()));

        p.setPrecio(new BigDecimal(txtPrecioPro.getText()));
        p.setCosto(new BigDecimal(txtCostoProducto.getText()));

        p.setFechaElaboracion(txtFechaElaboracion.getDate());
        p.setFechaCaducidad(txtFechaCaducidad.getDate());

        productosJpa.edit(p);

        JOptionPane.showMessageDialog(null, "Producto modificado");

        LimpiarTable(TableProducto);
        ListarProductos();
        LimpiarProductos();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error al modificar: " + e.getMessage());
    }
    }//GEN-LAST:event_btnEditarproActionPerformed

    private void btnExcelProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelProActionPerformed
        
        Excel.reporte();
    }//GEN-LAST:event_btnExcelProActionPerformed

    private void btnNuevoProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoProActionPerformed
        LimpiarProductos();
    }//GEN-LAST:event_btnNuevoProActionPerformed

    private void txtCodigoVentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoVentaKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

        if (!"".equals(txtCodigoVenta.getText())) {

            String cod = txtCodigoVenta.getText();

      
            Productos pro = productosJpa.findByCodigo(cod);

            if (pro != null) {
                txtDescripcionVenta.setText(pro.getNombre());
                txtPrecioVenta.setText(pro.getPrecio().toString());
                txtStockDisponible.setText(String.valueOf(pro.getStock()));
                txtCantidadVenta.requestFocus();
            } else {
                LimpiarVenta();
                JOptionPane.showMessageDialog(null, "Producto no encontrado");
                txtCodigoVenta.requestFocus();
            }

        } else {
            JOptionPane.showMessageDialog(null, "Ingrese el código del producto");
            txtCodigoVenta.requestFocus();
        }
    }

    }//GEN-LAST:event_txtCodigoVentaKeyPressed

    private void txtCantidadVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCantidadVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadVentaActionPerformed

    private void txtCantidadVentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadVentaKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(!"".equals(txtCantidadVenta.getText())){
                String cod = txtCodigoVenta.getText();
                String descripcion = txtDescripcionVenta.getText();
                int cant = Integer.parseInt(txtCantidadVenta.getText());
                double precio = Double.parseDouble(txtPrecioVenta.getText());
                double total = cant*precio;
                int stock = Integer.parseInt(txtStockDisponible.getText());
                if(stock>=cant){
                    item=item+1;
                    tmp =(DefaultTableModel) TablaVenta.getModel();
                    for (int i = 0; i < TablaVenta.getRowCount(); i++) {
                        if(TablaVenta.getValueAt(i, 1).equals(txtDescripcionVenta.getText())){
                        JOptionPane.showMessageDialog(null, "El producto ya esta registrado");
                        return;
                        }
                    }
                    ArrayList lista = new ArrayList();
                    lista.add(item);
                    lista.add(cod);
                    lista.add(descripcion);
                    lista.add(cant);
                    lista.add(precio);
                    lista.add(total);
                    Object[] O = new Object[5];
                    O[0]=lista.get(1);
                    O[1]=lista.get(2);
                    O[2]=lista.get(3);
                    O[3]=lista.get(4);
                    O[4]=lista.get(5);
                    tmp.addRow(O);
                    TablaVenta.setModel(tmp);
                    TotalPagar();
                    LimpiarVenta();
                    txtCodigoVenta.requestFocus();
                }else{
                    JOptionPane.showMessageDialog(null, "Stock no disponible");
                }
            }else{
                JOptionPane.showMessageDialog(null, "Ingrese cantidad");
            }
        }
    }//GEN-LAST:event_txtCantidadVentaKeyPressed

    private void btnEliminarventaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarventaActionPerformed
        modelo=(DefaultTableModel) TablaVenta.getModel();
        modelo.removeRow(TablaVenta.getSelectedRow());
        TotalPagar();
        txtCodigoVenta.requestFocus();
    }//GEN-LAST:event_btnEliminarventaActionPerformed

    private void txtRucVentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRucVentaKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

        if (!"".equals(txtRucVenta.getText())) {

            try {
                int dni = Integer.parseInt(txtRucVenta.getText());

                // Buscar usando JPA
                Clientes clienteJpa = clientesJpa.findByDni(dni);

                if (clienteJpa != null) {
                    txtNombreClienteventa.setText(clienteJpa.getNombre());
                    txtTelefonoCV.setText(String.valueOf(clienteJpa.getTelefono()));
                    txtDireccionCV.setText(clienteJpa.getDireccion());
                    txtRazonCV.setText(clienteJpa.getRazon());
                } else {
                    txtRucVenta.setText("");
                    JOptionPane.showMessageDialog(null, "El cliente no existe");
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "RUC inválido");
            }
        }
    }

    }//GEN-LAST:event_txtRucVentaKeyPressed

    private void btnGenerarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarVentaActionPerformed

    if (TablaVenta.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "No hay productos en la venta");
        return;
    }

    if ("".equals(txtNombreClienteventa.getText())) {
        JOptionPane.showMessageDialog(this, "Debes buscar un cliente");
        return;
    }

    int idVenta = RegistrarVentaJPA();
    if (idVenta == -1) return;

    RegistrarDetalleJPA(idVenta);

    ActualizarStockJPA();

    pdf();

    LimpiarTablaVenta();
    LimpiarClienteVenta();

    JOptionPane.showMessageDialog(this, "Venta registrada correctamente");

    }//GEN-LAST:event_btnGenerarVentaActionPerformed

    private void btnNuevaVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaVentaActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_btnNuevaVentaActionPerformed

    private void txtCodigoVentaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoVentaKeyTyped
        event.numberKeyPress(evt);
    }//GEN-LAST:event_txtCodigoVentaKeyTyped

    private void txtDescripcionVentaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescripcionVentaKeyTyped
        event.textKeyPress(evt);
    }//GEN-LAST:event_txtDescripcionVentaKeyTyped

    private void txtCantidadVentaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadVentaKeyTyped
        event.numberKeyPress(evt);
    }//GEN-LAST:event_txtCantidadVentaKeyTyped

    private void txtPrecioProKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPrecioProKeyTyped
        event.numberDecimalKeyPress(evt, txtPrecioPro);
           char c = evt.getKeyChar();

    if (!Character.isDigit(c)) {
        evt.consume();
        return;
    }

    if (txtCantPro.getText().length() >= 4) {
        evt.consume();
    }
    }//GEN-LAST:event_txtPrecioProKeyTyped

    private void btnActualizarConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarConfigActionPerformed
    try {
        Config c = configJpa.findConfig(1);  

        if (c == null) {
            JOptionPane.showMessageDialog(null, 
                "No existe configuración inicial. Debes crearla primero.");
            return;
        }
        c.setRuc(txtRucConfig.getText());
        c.setNombre(txtNombreConfig.getText());
        c.setTelefono(Integer.parseInt(txtTelefonoConfig.getText()));
        c.setDireccion(txtDireccionConfig.getText());
        c.setRazon(txtRazonConfig.getText());
        configJpa.edit(c);

        JOptionPane.showMessageDialog(null, "Datos de configuración actualizados");

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
    }


    }//GEN-LAST:event_btnActualizarConfigActionPerformed
private void CrearConfigInicial() {
    if (configJpa.findConfig(1) == null) {

        Config c = new Config();
        c.setNombre("");
        c.setRuc("");
        c.setTelefono(0);
        c.setDireccion("");
        c.setRazon("");

        try {
            configJpa.create(c);
        } catch (Exception e) {
            System.out.println("Error creando config inicial: " + e.getMessage());
        }
    }
}

    private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentasActionPerformed
        jTabbedPane1.setSelectedIndex(4);
        LimpiarTable(TableVentas);
        ListarVentas();
    }//GEN-LAST:event_btnVentasActionPerformed

    private void TableVentasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableVentasMouseClicked
        int fila = TableVentas.rowAtPoint(evt.getPoint());
        txtIdVenta.setText(TableVentas.getValueAt(fila, 0).toString());
    }//GEN-LAST:event_TableVentasMouseClicked

    private void btnPdfVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfVentasActionPerformed
        try {
            int id = Integer.parseInt(txtIdVenta.getText());
            File file = new File("src/pdf/venta"+id+".pdf");
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnPdfVentasActionPerformed

    private void btnGraficarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGraficarActionPerformed
        
        String fechaReporte = new SimpleDateFormat("dd-MM-yyyy").format(Midate.getDate());
        Grafico.Graficar(fechaReporte);
    }//GEN-LAST:event_btnGraficarActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Login iniciar = new Login();
        iniciar.setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void txtRazonProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRazonProveedorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRazonProveedorActionPerformed

    private void txtCodigoProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoProActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoProActionPerformed

    private void txtDniClienteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDniClienteKeyTyped
    char c = evt.getKeyChar();

    if (!Character.isDigit(c)) {
        evt.consume();
    }

    if (txtDniCliente.getText().length() >= 10) {
        evt.consume();
    }
    }//GEN-LAST:event_txtDniClienteKeyTyped

    private void txtNombreClienteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNombreClienteKeyTyped
    char c = evt.getKeyChar();

    
    if (!Character.isLetter(c) && c != ' ') {
        evt.consume(); 
    }
    }//GEN-LAST:event_txtNombreClienteKeyTyped

    private void txtTelefonoClienteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTelefonoClienteKeyTyped
    char c = evt.getKeyChar();

    if (!Character.isDigit(c)) {
        evt.consume();
        return;
    }

    if (txtTelefonoCliente.getText().length() >= 10) {
        evt.consume();
    }
    }//GEN-LAST:event_txtTelefonoClienteKeyTyped

    private void txtDireccionClienteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDireccionClienteKeyTyped
    char c = evt.getKeyChar();
    if (Character.isLetterOrDigit(c) || c == ' ' || c == '#' || c == '-' || c == '.' || c == ',') {
    } else {
        evt.consume(); 
    }
    }//GEN-LAST:event_txtDireccionClienteKeyTyped

    private void txtRucProveedorKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRucProveedorKeyTyped
    char c = evt.getKeyChar();

    
    if (!Character.isDigit(c)) {
        evt.consume();
        return;
    }

    if (txtRucProveedor.getText().length() >= 13) {
        evt.consume();
    }


    }//GEN-LAST:event_txtRucProveedorKeyTyped

    private void txtNombreproveedorKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNombreproveedorKeyTyped
char c = evt.getKeyChar();

    
    if (!Character.isLetter(c) && c != ' ') {
        evt.consume(); 
    }
    }//GEN-LAST:event_txtNombreproveedorKeyTyped

    private void txtTelefonoProveedorKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTelefonoProveedorKeyTyped
 char c = evt.getKeyChar();

    if (!Character.isDigit(c)) {
        evt.consume();
        return;
    }

    if (txtTelefonoCliente.getText().length() >= 10) {
        evt.consume();
    }
    }//GEN-LAST:event_txtTelefonoProveedorKeyTyped

    private void txtDireccionProveedorKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDireccionProveedorKeyTyped
    char c = evt.getKeyChar();
    if (Character.isLetterOrDigit(c) || c == ' ' || c == '#' || c == '-' || c == '.' || c == ',') {
    } else {
        evt.consume(); 
    }
    }//GEN-LAST:event_txtDireccionProveedorKeyTyped

    private void txtCantProKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantProKeyTyped
    char c = evt.getKeyChar();

    if (!Character.isDigit(c)) {
        evt.consume();
        return;
    }

    if (txtCantPro.getText().length() >= 4) {
        evt.consume();
    }

    }//GEN-LAST:event_txtCantProKeyTyped

    private void txtRucConfigKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRucConfigKeyTyped
  char c = evt.getKeyChar();

    if (!Character.isDigit(c)) {
        evt.consume();
    }

    if (txtDniCliente.getText().length() >= 10) {
        evt.consume();
    }
    }//GEN-LAST:event_txtRucConfigKeyTyped

    private void txtNombreConfigKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNombreConfigKeyTyped
 char c = evt.getKeyChar();

    
    if (!Character.isLetter(c) && c != ' ') {
        evt.consume(); 
    }
    }//GEN-LAST:event_txtNombreConfigKeyTyped

    private void txtTelefonoConfigKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTelefonoConfigKeyTyped
  char c = evt.getKeyChar();

    if (!Character.isDigit(c)) {
        evt.consume();
        return;
    }

    if (txtTelefonoCliente.getText().length() >= 10) {
        evt.consume();
    }
    }//GEN-LAST:event_txtTelefonoConfigKeyTyped

    private void btnNuevaVenta1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaVenta1ActionPerformed
        jTabbedPane1.setSelectedIndex(6);
    }//GEN-LAST:event_btnNuevaVenta1ActionPerformed

    private void btnNuevaVenta2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaVenta2ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_btnNuevaVenta2ActionPerformed

    private void btnClientes1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClientes1ActionPerformed
LimpiarTable(TableCliente);
        ListarCliente();
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_btnClientes1ActionPerformed

    private void btnProveedor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProveedor1ActionPerformed
LimpiarTable(TableProveedor);
        ListarProveedor();
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_btnProveedor1ActionPerformed

    private void btnProductos1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductos1ActionPerformed
 LimpiarTable(TableProducto);
        ListarProductos();
        jTabbedPane1.setSelectedIndex(3);    }//GEN-LAST:event_btnProductos1ActionPerformed

    private void btnVentas1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentas1ActionPerformed
  jTabbedPane1.setSelectedIndex(4);
        LimpiarTable(TableVentas);
        ListarVentas();    }//GEN-LAST:event_btnVentas1ActionPerformed

    private void btnConfig1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfig1ActionPerformed
        jTabbedPane1.setSelectedIndex(5);
    }//GEN-LAST:event_btnConfig1ActionPerformed
public void ListarPorAgotarse() {
    List<Productos> lista = productosJpa.findProductosEntities();
    modelo = (DefaultTableModel) TableAlertaStock.getModel();
    modelo.setRowCount(0);

    Object[] ob = new Object[4];

    for (Productos p : lista) {
        if (p.getStock() <= p.getStockMin()) {
            ob[0] = p.getNombre();
            ob[1] = p.getStock();
            ob[2] = p.getStockMin();
            ob[3] = p.getProveedor();
            modelo.addRow(ob);
        }
    }
}

public void ListarPorVencer() {

    List<Productos> lista = productosJpa.findProductosEntities();
    DefaultTableModel modelo = (DefaultTableModel) TablePorVencer.getModel();
    modelo.setRowCount(0);

    Date hoy = new Date();
    long tresDias = 3L * 24 * 60 * 60 * 1000; 

    for (Productos p : lista) {

        // Evitamos NullPointerException
        if (p.getFechaCaducidad() == null) {
            continue;
        }

        long diff = p.getFechaCaducidad().getTime() - hoy.getTime();
        if (diff >= 0 && diff <= tresDias) {

            modelo.addRow(new Object[]{
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getStock(),
                new SimpleDateFormat("dd-MM-yyyy").format(p.getFechaCaducidad())
            });
        }
    }
}



public void ProcesarProductosVencidos() {

    List<Productos> lista = productosJpa.findProductosEntities();
    Date hoy = new Date();

    for (Productos p : lista) {
        if (p.getFechaCaducidad() == null) {
            continue;
        }

        
        if (p.getFechaCaducidad().before(hoy) && p.getStock() > 0) {

            try {
                
                Perdidas perdida = new Perdidas();
                perdida.setIdProducto(p);
                perdida.setNombreProducto(p.getNombre());
                perdida.setCantidad(p.getStock());
                perdida.setCostoUnitario(p.getCosto());
                perdida.setCostoTotal(
                    p.getCosto().multiply(new BigDecimal(p.getStock()))
                );
                perdida.setMotivo("VENCIDO");
                perdida.setFecha(hoy);

                perdidasJpa.create(perdida);

                
                p.setStock(0);
                productosJpa.edit(p);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Error procesando vencidos: " + ex.getMessage());
            }
        }
    }

    JOptionPane.showMessageDialog(null, "Inventario actualizado. Productos vencidos registrados.");
}



public void ListarPerdidas() {
    List<Perdidas> lista = perdidasJpa.findPerdidasEntities();
    DefaultTableModel modelo = (DefaultTableModel) TablePerdidas.getModel();
    modelo.setRowCount(0);

    for (Perdidas p : lista) {
        modelo.addRow(new Object[]{
            p.getNombreProducto(),
            p.getCantidad(),
            p.getCostoUnitario(),
            p.getCostoTotal(),
            p.getMotivo(),
            p.getFecha()
        });
    }
        calcularTotalPerdidas();

}


    private void btnAlertaStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlertaStockActionPerformed
       jTabbedPane1.setSelectedIndex(7);
        ListarPorAgotarse();
    }//GEN-LAST:event_btnAlertaStockActionPerformed

    private void btnProductos2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductos2ActionPerformed
 LimpiarTable(TableProducto);
        ListarProductos();
        jTabbedPane1.setSelectedIndex(3);    }//GEN-LAST:event_btnProductos2ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
     
               ProcesarProductosVencidos();

        jTabbedPane1.setSelectedIndex(8);
       
        ListarPorVencer();
    ListarPerdidas();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnProductos3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductos3ActionPerformed
              LimpiarTable(TableProducto);
        ListarProductos();
        jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_btnProductos3ActionPerformed

    private void btnBuscarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarProductoActionPerformed
    String nombre = txtBuscarProducto.getText().trim();

    if (nombre.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ingrese el nombre del producto");
        return;
    }

    BuscarProducto dialog = new BuscarProducto(
        this,
        true,
        this,
        nombre
    );
    dialog.setVisible(true);
    }//GEN-LAST:event_btnBuscarProductoActionPerformed

    public void cargarProductoDesdeBusqueda(String codigo, String nombre, String precio, String stock) {

    txtCodigoVenta.setText(codigo);
    txtDescripcionVenta.setText(nombre);
    txtPrecioVenta.setText(precio);
    txtStockDisponible.setText(stock); // 👈 AQUÍ

    txtCantidadVenta.requestFocus();
}


    private void TotalPagar(){
        Totalpagar=0.00;
        int numFila = TablaVenta.getRowCount();
        for (int i = 0; i < numFila; i++) {
            double cal = Double.parseDouble(String.valueOf(TablaVenta.getModel().getValueAt(i, 4)));
            Totalpagar = Totalpagar + cal;
        }
        LabelTotal.setText(String.format("%.2f", Totalpagar));
    }
  public void calcularTotalPerdidas() {

    double total = 0;

    for (int i = 0; i < TablePerdidas.getRowCount(); i++) {

        Object valor = TablePerdidas.getValueAt(i, 3); 

        if (valor == null) {
            continue;
        }

        try {
            total += Double.parseDouble(valor.toString());
        } catch (NumberFormatException e) {
            System.out.println("Valor no numérico en fila " + i + ": " + valor);
        }
    }

    lblTotalPerdidas.setText(String.format("%.2f", total));
}


 public static boolean validarCedula(String cedula) {
    if (cedula == null || !cedula.matches("\\d{10}")) return false;

    int provincia = Integer.parseInt(cedula.substring(0, 2));
    if (provincia < 1 || provincia > 24) return false;

    int[] coef = {2,1,2,1,2,1,2,1,2};
    int suma = 0;

    for (int i = 0; i < 9; i++) {
        int valor = Character.getNumericValue(cedula.charAt(i)) * coef[i];
        if (valor > 9) valor -= 9;
        suma += valor;
    }

    int verificador = (10 - (suma % 10)) % 10;

    return verificador == Character.getNumericValue(cedula.charAt(9));
}

    


    
    private void LimpiarVenta(){
        txtCodigoVenta.setText("");
        txtDescripcionVenta.setText("");
        txtCantidadVenta.setText("");
        txtStockDisponible.setText("");
        txtPrecioVenta.setText("");
        txtIdVenta.setText("");
    }
   
    private void RegistrarVenta(){
        String cliente = txtNombreClienteventa.getText();
        String vendedor = LabelVendedor.getText();
        double monto = Totalpagar;
        v.setCliente(cliente);
        v.setVendedor(vendedor);
        v.setTotal(monto);
        v.setFecha(fechaActual);
        Vdao.RegistrarVenta(v);
    }
    

    
    private void LimpiarTablaVenta(){
         tmp=(DefaultTableModel) TablaVenta.getModel();
         int fila = TablaVenta.getRowCount();
         for (int i = 0; i < fila; i++) {
            tmp.removeRow(0);
        }
    }
    
    private void LimpiarClienteVenta(){
        txtRucVenta.setText("");
        txtNombreClienteventa.setText("");
        txtTelefonoCV.setText("");
        txtDireccionCV.setText("");
        txtRazonCV.setText("");
    }
    
   public void ListarConfig(){
    Config conf = configJpa.findConfig(1); 

    if (conf != null) {
        txtRucConfig.setText(conf.getRuc());
        txtNombreConfig.setText(conf.getNombre());
        txtTelefonoConfig.setText(String.valueOf(conf.getTelefono()));
        txtDireccionConfig.setText(conf.getDireccion());
        txtRazonConfig.setText(conf.getRazon());
    }
}



    
    private void pdf(){
        try {
            int id = Vdao.IdVenta();
            FileOutputStream archivo;
            File file = new File("src/pdf/venta"+id+".pdf");
            archivo = new FileOutputStream(file);
            Document doc = new Document() ;
            PdfWriter.getInstance(doc, archivo);
            doc.open();
            Image img = Image.getInstance("src/img/login.png");
            
            Paragraph fecha = new Paragraph();
            Font negrita = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);
            fecha.add(Chunk.NEWLINE);
            Date date = new Date();
            fecha.add("Factura:" +id+"\n"+ "Fecha: "+ new SimpleDateFormat("dd-MM-yyyy").format(date)+"\n\n");
            
            PdfPTable Encabezado = new PdfPTable(4);
            Encabezado.setWidthPercentage(100);
            Encabezado.getDefaultCell().setBorder(0);
            float[] ColumnaEncabezado = new float[]{20f, 30f, 70f, 40f};
            Encabezado.setWidths(ColumnaEncabezado);
            Encabezado.setHorizontalAlignment(Element.ALIGN_LEFT);
            
            Encabezado.addCell(img);
            
            String ruc = txtRucConfig.getText();
            String nom = txtNombreConfig.getText();
            String tel = txtTelefonoConfig.getText();
            String dir = txtDireccionConfig.getText();
            String ra = txtRazonConfig.getText();
            
            Encabezado.addCell("");
            Encabezado.addCell("Ruc: "+ruc+ "\nNombre: "+nom+ "\nTelefono: "+tel+ "\nDireccion: "+dir+ "\nRazon: "+ra);
            Encabezado.addCell(fecha);
            doc.add(Encabezado);
            
            Paragraph cli = new Paragraph();
            cli.add(Chunk.NEWLINE);
            cli.add("Datos de los clientes"+"\n\n");
            doc.add(cli);
            
            PdfPTable tablacli = new PdfPTable(4);
            tablacli.setWidthPercentage(100);
            tablacli.getDefaultCell().setBorder(0);
            float[] Columnacli = new float[]{10f, 50f, 15f, 20f};
            tablacli.setWidths(Columnacli);
            tablacli.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell cl1 = new PdfPCell(new Phrase("Dni/Ruc", negrita));
            PdfPCell cl2 = new PdfPCell(new Phrase("Nombre", negrita));
            PdfPCell cl3 = new PdfPCell(new Phrase("Telefono", negrita));
            PdfPCell cl4 = new PdfPCell(new Phrase("Direccion", negrita));
            cl1.setBorder(0);
            cl2.setBorder(0);
            cl3.setBorder(0);
            cl4.setBorder(0);
            tablacli.addCell(cl1);
            tablacli.addCell(cl2);
            tablacli.addCell(cl3);
            tablacli.addCell(cl4);
            tablacli.addCell(txtRucVenta.getText());
            tablacli.addCell(txtNombreClienteventa.getText());
            tablacli.addCell(txtTelefonoCV.getText());
            tablacli.addCell(txtDireccionCV.getText());
            
            doc.add(tablacli);
            
            //productos
            PdfPTable tablapro = new PdfPTable(4);
            tablapro.setWidthPercentage(100);
            tablapro.getDefaultCell().setBorder(0);
            float[] Columnapro = new float[]{20f, 50f, 30f, 40f};
            tablapro.setWidths(Columnapro);
            tablacli.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell pro1 = new PdfPCell(new Phrase("Cant.", negrita));
            PdfPCell pro2 = new PdfPCell(new Phrase("Descripción", negrita));
            PdfPCell pro3 = new PdfPCell(new Phrase("Precio U.", negrita));
            PdfPCell pro4 = new PdfPCell(new Phrase("Precio T.", negrita));
            pro1.setBorder(0);
            pro2.setBorder(0);
            pro3.setBorder(0);
            pro4.setBorder(0);
            pro1.setBackgroundColor(BaseColor.WHITE);
            pro2.setBackgroundColor(BaseColor.WHITE);
            pro3.setBackgroundColor(BaseColor.WHITE);
            pro4.setBackgroundColor(BaseColor.WHITE);
            tablapro.addCell(pro1);
            tablapro.addCell(pro2);
            tablapro.addCell(pro3);
            tablapro.addCell(pro4);
            for (int i = 0; i < TablaVenta.getRowCount(); i++) {
                String producto = TablaVenta.getValueAt(i, 1).toString();
                String cantidad = TablaVenta.getValueAt(i, 2).toString();
                String precio = TablaVenta.getValueAt(i, 3).toString();
                String total = TablaVenta.getValueAt(i, 4).toString();
                tablapro.addCell(cantidad);
                tablapro.addCell(producto);
                tablapro.addCell(precio);
                tablapro.addCell(total);
            
            }
            doc.add(tablapro);
            
            Paragraph info = new Paragraph();
            info.add(Chunk.NEWLINE);
            info.add("Total a pagar"+Totalpagar);
            info.setAlignment(Element.ALIGN_RIGHT);
            doc.add(info);
            
            Paragraph firma = new Paragraph();
            firma.add(Chunk.NEWLINE);
            firma.add("Cancelación y Firma\n\n");
            firma.add("------------------------");
            firma.setAlignment(Element.ALIGN_CENTER);
            doc.add(firma);
            
            Paragraph mensaje = new Paragraph();
            mensaje.add(Chunk.NEWLINE);
            mensaje.add("Gracias por su compra");
            mensaje.setAlignment(Element.ALIGN_CENTER);
            doc.add(mensaje);
            doc.close();
            archivo.close();
            Desktop.getDesktop().open(file);
        } catch (DocumentException | IOException e) {
            System.out.println(e.toString());
        }
    }
    
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

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Sistema().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelTotal;
    private javax.swing.JLabel LabelVendedor;
    private com.toedter.calendar.JDateChooser Midate;
    private javax.swing.JTable TablaVenta;
    private javax.swing.JTable TableAlertaStock;
    private javax.swing.JTable TableCliente;
    private javax.swing.JTable TablePerdidas;
    private javax.swing.JTable TablePorVencer;
    private javax.swing.JTable TableProducto;
    private javax.swing.JTable TableProveedor;
    private javax.swing.JTable TableVentas;
    private javax.swing.JButton btnActualizarConfig;
    private javax.swing.JButton btnAlertaStock;
    private javax.swing.JButton btnBuscarProducto;
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnClientes1;
    private javax.swing.JButton btnConfig;
    private javax.swing.JButton btnConfig1;
    private javax.swing.JButton btnEditarCliente;
    private javax.swing.JButton btnEditarProveedor;
    private javax.swing.JButton btnEditarpro;
    private javax.swing.JButton btnEliminarCliente;
    private javax.swing.JButton btnEliminarPro;
    private javax.swing.JButton btnEliminarProveedor;
    private javax.swing.JButton btnEliminarventa;
    private javax.swing.JButton btnExcelPro;
    private javax.swing.JButton btnGenerarVenta;
    private javax.swing.JButton btnGraficar;
    private javax.swing.JButton btnGuardarCliente;
    private javax.swing.JButton btnGuardarpro;
    private javax.swing.JButton btnNuevaVenta;
    private javax.swing.JButton btnNuevaVenta1;
    private javax.swing.JButton btnNuevaVenta2;
    private javax.swing.JButton btnNuevoCliente;
    private javax.swing.JButton btnNuevoPro;
    private javax.swing.JButton btnNuevoProveedor;
    private javax.swing.JButton btnPdfVentas;
    private javax.swing.JButton btnProductos;
    private javax.swing.JButton btnProductos1;
    private javax.swing.JButton btnProductos2;
    private javax.swing.JButton btnProductos3;
    private javax.swing.JButton btnProveedor;
    private javax.swing.JButton btnProveedor1;
    private javax.swing.JButton btnVentas;
    private javax.swing.JButton btnVentas1;
    private javax.swing.JButton btnguardarProveedor;
    private javax.swing.JComboBox<String> cbEstadoProveedor;
    private javax.swing.JComboBox<String> cbxProveedorPro;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblTotalPerdidas;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtCantPro;
    private javax.swing.JTextField txtCantidadVenta;
    private javax.swing.JTextField txtCodigoPro;
    private javax.swing.JTextField txtCodigoVenta;
    private javax.swing.JTextField txtCorreoProveedor;
    private javax.swing.JTextField txtCostoProducto;
    private javax.swing.JTextField txtDesPro;
    private javax.swing.JTextField txtDescripcionVenta;
    private javax.swing.JTextField txtDireccionCV;
    private javax.swing.JTextField txtDireccionCliente;
    private javax.swing.JTextField txtDireccionConfig;
    private javax.swing.JTextField txtDireccionProveedor;
    private javax.swing.JTextField txtDniCliente;
    private com.toedter.calendar.JDateChooser txtFechaCaducidad;
    private com.toedter.calendar.JDateChooser txtFechaElaboracion;
    private javax.swing.JTextField txtIdCliente;
    private javax.swing.JTextField txtIdConfig;
    private javax.swing.JTextField txtIdPro;
    private javax.swing.JTextField txtIdProveedor;
    private javax.swing.JTextField txtIdVenta;
    private javax.swing.JTextField txtIdpro;
    private javax.swing.JTextField txtNombreCliente;
    private javax.swing.JTextField txtNombreClienteventa;
    private javax.swing.JTextField txtNombreConfig;
    private javax.swing.JTextField txtNombreproveedor;
    private javax.swing.JTextArea txtObservacionesProveedor;
    private javax.swing.JTextField txtPrecioPro;
    private javax.swing.JTextField txtPrecioVenta;
    private javax.swing.JTextField txtRazonCV;
    private javax.swing.JTextField txtRazonCliente;
    private javax.swing.JTextField txtRazonConfig;
    private javax.swing.JTextField txtRazonProveedor;
    private javax.swing.JTextField txtRucConfig;
    private javax.swing.JTextField txtRucProveedor;
    private javax.swing.JTextField txtRucVenta;
    private javax.swing.JTextField txtStockDisponible;
    private javax.swing.JTextField txtStockMinProducto;
    private javax.swing.JTextField txtTelefonoCV;
    private javax.swing.JTextField txtTelefonoCliente;
    private javax.swing.JTextField txtTelefonoConfig;
    private javax.swing.JTextField txtTelefonoProveedor;
    private javax.swing.JTextField txtVendedor;
    // End of variables declaration//GEN-END:variables

}
