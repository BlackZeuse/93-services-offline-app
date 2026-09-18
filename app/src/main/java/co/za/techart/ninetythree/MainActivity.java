package co.za.techart.ninetythree;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends Activity {
    static final int ORANGE = Color.rgb(242, 140, 0);
    static final int BLACK = Color.rgb(17, 17, 17);
    static final int LIGHT = Color.rgb(247, 247, 247);
    static final int LINE = Color.rgb(218, 218, 218);
    static final int WHITE = Color.WHITE;

    LinearLayout content;
    SharedPreferences prefs;
    String documentType = "INVOICE";
    ArrayList<Item> items = new ArrayList<>();
    ArrayList<EditText> descFields = new ArrayList<>();
    ArrayList<EditText> qtyFields = new ArrayList<>();
    ArrayList<EditText> priceFields = new ArrayList<>();
    ArrayList<TextView> totalLabels = new ArrayList<>();
    ArrayList<String> defaultServices = new ArrayList<>();
    HashMap<String, Double> servicePrices = new HashMap<>();

    EditText customerName, customerPhone, customerEmail, customerAddress, jobRef, vehicleReg;
    EditText discountField, vatRateField, paidField;
    CheckBox vatCheck;
    Spinner paymentStatus;
    TextView subtotalLabel, vatLabel, grandTotalLabel, balanceLabel, selectedServicesLabel;
    Button serviceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("93services", MODE_PRIVATE);
        loadServices();
        showSplash();
    }

    int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    TextView tv(String text, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, 1);
        t.setPadding(dp(2), dp(2), dp(2), dp(2));
        return t;
    }

    Button btn(String text, boolean filled) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTextColor(filled ? WHITE : ORANGE);
        b.setTypeface(null, 1);
        b.setBackgroundResource(filled ? R.drawable.bg_button : R.drawable.bg_outline);
        b.setMinHeight(dp(52));
        return b;
    }

    EditText input(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(15);
        e.setTextColor(BLACK);
        e.setHintTextColor(Color.GRAY);
        e.setInputType(type);
        e.setSingleLine(true);
        e.setBackgroundResource(R.drawable.bg_input);
        e.setPadding(dp(12), 0, dp(12), 0);
        return e;
    }

    View space(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h)));
        return v;
    }

    LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    LinearLayout.LayoutParams full(int h) {
        return new LinearLayout.LayoutParams(-1, dp(h));
    }

    void setupRoot(boolean back) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(LIGHT);

        if (back) {
            LinearLayout top = row();
            top.setPadding(dp(10), dp(8), dp(10), dp(6));
            Button backBtn = btn("‹", false);
            backBtn.setTextSize(30);
            backBtn.setPadding(0, 0, 0, 0);
            backBtn.setOnClickListener(v -> showHome());
            top.addView(backBtn, new LinearLayout.LayoutParams(dp(54), dp(52)));
            TextView title = tv("93 SERVICES", 20, BLACK, true);
            title.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, dp(52), 1);
            tp.leftMargin = dp(10);
            top.addView(title, tp);
            root.addView(top);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(10), dp(18), dp(26));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    void showSplash() {
        LinearLayout s = new LinearLayout(this);
        s.setOrientation(LinearLayout.VERTICAL);
        s.setGravity(Gravity.CENTER);
        s.setBackgroundColor(WHITE);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        s.addView(logo, new LinearLayout.LayoutParams(dp(300), dp(220)));

        TextView name = tv("93 SERVICES", 28, BLACK, true);
        name.setGravity(Gravity.CENTER);
        s.addView(name);
        TextView by = tv("Designed & Developed by TECHART (PTY) LTD", 12, Color.DKGRAY, false);
        by.setGravity(Gravity.CENTER);
        by.setPadding(0, dp(16), 0, 0);
        s.addView(by);
        setContentView(s);

        new android.os.Handler().postDelayed(this::showHome, 900);
    }

    void showHome() {
        items.clear();
        setupRoot(false);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(155));
        lp.gravity = Gravity.CENTER;
        content.addView(logo, lp);

        TextView tag = tv("OFFLINE BUSINESS APP", 14, ORANGE, true);
        tag.setGravity(Gravity.CENTER);
        content.addView(tag);
        content.addView(space(12));

        TextView q = tv("What do you want to create?", 23, BLACK, true);
        q.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(q);
        content.addView(space(16));

        Button inv = btn("Create Invoice", true);
        inv.setOnClickListener(v -> {
            documentType = "INVOICE";
            startNewDocument();
        });
        content.addView(inv, full(56));
        content.addView(space(12));

        Button quote = btn("Create Quote", true);
        quote.setOnClickListener(v -> {
            documentType = "QUOTE";
            startNewDocument();
        });
        content.addView(quote, full(56));
        content.addView(space(22));

        Button history = btn("Document History", false);
        history.setOnClickListener(v -> showHistory());
        content.addView(history, full(54));
        content.addView(space(10));

        Button customers = btn("Customers", false);
        customers.setOnClickListener(v -> showCustomers());
        content.addView(customers, full(54));
        content.addView(space(10));

        Button services = btn("Services & Prices", false);
        services.setOnClickListener(v -> showServiceSettings());
        content.addView(services, full(54));
        content.addView(space(10));

        Button backup = btn("Backup / Restore", false);
        backup.setOnClickListener(v -> showBackup());
        content.addView(backup, full(54));
        content.addView(space(18));

        TextView foot = tv("Designed & Developed by TECHART (PTY) LTD", 11, Color.GRAY, false);
        foot.setGravity(Gravity.CENTER);
        content.addView(foot);
    }

    void startNewDocument() {
        items.clear();
        showServiceSelector();
    }

    void showServiceSelector() {
        setupRoot(true);
        TextView head = tv(documentType + " - SERVICES", 24, BLACK, true);
        content.addView(head);
        content.addView(space(8));
        content.addView(tv("Select one or more services. Default prices can be changed later.", 14, Color.DKGRAY, false));
        content.addView(space(14));

        serviceButton = btn("Select Services ▾", false);
        serviceButton.setOnClickListener(v -> openServiceDialog());
        content.addView(serviceButton, full(56));
        content.addView(space(8));

        selectedServicesLabel = tv("No services selected", 14, Color.DKGRAY, false);
        selectedServicesLabel.setGravity(Gravity.CENTER_VERTICAL);
        selectedServicesLabel.setBackgroundResource(R.drawable.bg_card);
        selectedServicesLabel.setPadding(dp(12), dp(8), dp(12), dp(8));
        content.addView(selectedServicesLabel, full(76));
        content.addView(space(12));

        Button manual = btn("+ Add Manual Item", false);
        manual.setOnClickListener(v -> {
            addManualItem();
            Toast.makeText(this, "Manual item added.", Toast.LENGTH_SHORT).show();
            refreshSelectionText();
        });
        content.addView(manual, full(54));
        content.addView(space(14));

        Button cont = btn("Continue", true);
        cont.setOnClickListener(v -> showEditor());
        content.addView(cont, full(56));
    }

    void openServiceDialog() {
        final String[] names = defaultServices.toArray(new String[0]);
        final boolean[] checked = new boolean[names.length];
        for (int i = 0; i < names.length; i++) checked[i] = containsNamedItem(names[i]);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Services")
                .setMultiChoiceItems(names, checked, (d, which, isChecked) -> {
                    if (isChecked && !containsNamedItem(names[which])) {
                        addItem(names[which], servicePrices.getOrDefault(names[which], 0.0));
                    } else if (!isChecked) {
                        removeNamedItem(names[which]);
                    }
                    refreshSelectionText();
                })
                .setPositiveButton("Done", null)
                .setNeutralButton("Add Manually", null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            addManualItem();
            refreshSelectionText();
        }));
        dialog.show();
    }

    boolean containsNamedItem(String name) {
        for (Item x : items) if (name.equalsIgnoreCase(x.description)) return true;
        return false;
    }

    void addItem(String name, double price) {
        items.add(new Item(name, 1, price));
    }

    void removeNamedItem(String name) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (name.equalsIgnoreCase(items.get(i).description)) items.remove(i);
        }
    }

    void addManualItem() {
        items.add(new Item("", 1, 0));
    }

    void refreshSelectionText() {
        if (selectedServicesLabel == null) return;
        ArrayList<String> names = new ArrayList<>();
        for (Item x : items) if (!TextUtils.isEmpty(x.description.trim())) names.add(x.description.trim());
        selectedServicesLabel.setText(names.isEmpty() ? "No services selected" : TextUtils.join("  •  ", names));
    }

    void showEditor() {
        if (items.isEmpty()) {
            Toast.makeText(this, "Select at least one service or add a manual item.", Toast.LENGTH_SHORT).show();
            return;
        }

        setupRoot(true);
        TextView head = tv(documentType, 25, BLACK, true);
        content.addView(head);
        content.addView(space(6));
        TextView hint = tv("Complete the customer and job details, then generate the PDF.", 13, Color.DKGRAY, false);
        content.addView(hint);
        content.addView(space(12));

        addCustomerSection();
        addItemsSection();
        addTotalsSection();
        applyPendingEditValues();

        content.addView(space(16));
        Button preview = btn("Preview PDF", true);
        preview.setOnClickListener(v -> generatePdfAndShare(false));
        content.addView(preview, full(56));
        content.addView(space(10));
        Button share = btn("Generate PDF & Share", false);
        share.setOnClickListener(v -> generatePdfAndShare(true));
        content.addView(share, full(56));
    }

    void addCustomerSection() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackgroundResource(R.drawable.bg_card);
        content.addView(card);

        card.addView(tv("CUSTOMER DETAILS", 16, BLACK, true));
        card.addView(space(8));

        JSONArray customers = getCustomersArray();

        customerName = input("Customer / Company name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        customerPhone = input("Phone number", InputType.TYPE_CLASS_PHONE);
        customerEmail = input("Email", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        customerAddress = input("Address", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        customerAddress.setSingleLine(false);
        customerAddress.setMinLines(2);
        customerAddress.setGravity(Gravity.TOP);
        jobRef = input("Job / Reference", InputType.TYPE_CLASS_TEXT);
        vehicleReg = input("Vehicle registration (optional)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        card.addView(customerName, full(48)); card.addView(space(8));
        card.addView(customerPhone, full(48)); card.addView(space(8));
        card.addView(customerEmail, full(48)); card.addView(space(8));
        card.addView(customerAddress, new LinearLayout.LayoutParams(-1, dp(72))); card.addView(space(8));
        card.addView(jobRef, full(48)); card.addView(space(8));
        card.addView(vehicleReg, full(48));

        if (customers.length() > 0) {
            TextView selectLabel = tv("SAVED CUSTOMER", 12, Color.DKGRAY, true);
            card.addView(space(10));
            card.addView(selectLabel);
            Spinner customerPicker = new Spinner(this);
            ArrayList<String> names = new ArrayList<>();
            names.add("Select saved customer…");
            for (int i = 0; i < customers.length(); i++) names.add(customers.optJSONObject(i).optString("name"));
            customerPicker.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
            customerPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) { }
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position <= 0) return;
                    JSONObject o = customers.optJSONObject(position - 1);
                    if (o == null) return;
                    customerName.setText(o.optString("name"));
                    customerPhone.setText(o.optString("phone"));
                    customerEmail.setText(o.optString("email"));
                    customerAddress.setText(o.optString("address"));
                }
            });
            card.addView(customerPicker, full(52));
        }

    }

    void addItemsSection() {
        content.addView(space(14));
        content.addView(tv("ITEMS", 16, BLACK, true));
        content.addView(space(6));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(10), dp(10), dp(10));
        box.setBackgroundResource(R.drawable.bg_card);
        content.addView(box);

        descFields.clear(); qtyFields.clear(); priceFields.clear(); totalLabels.clear();
        for (Item item : new ArrayList<>(items)) addEditorRow(box, item);

        Button add = btn("+ Add Item", false);
        add.setOnClickListener(v -> {
            Item i = new Item("", 1, 0);
            items.add(i);
            addEditorRow(box, i);
        });
        content.addView(space(10));
        content.addView(add, full(54));
    }

    void addEditorRow(LinearLayout parent, Item item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(12));
        card.setBackgroundColor(WHITE);

        LinearLayout top = row();
        EditText desc = input("Description", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        desc.setText(item.description);
        top.addView(desc, new LinearLayout.LayoutParams(0, dp(48), 1));

        Button remove = btn("Remove", false);
        remove.setTextSize(12);
        remove.setMinHeight(dp(42));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(82), dp(48));
        rp.leftMargin = dp(8);
        top.addView(remove, rp);
        card.addView(top);

        LinearLayout nums = row();
        nums.setPadding(0, dp(7), 0, 0);
        EditText qty = input("Qty", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        qty.setText(formatQty(item.qty));
        nums.addView(qty, new LinearLayout.LayoutParams(0, dp(46), 1));

        EditText price = input("Unit price", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (item.unitPrice > 0) price.setText(moneyPlain(item.unitPrice));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(0, dp(46), 1);
        pp.leftMargin = dp(8);
        nums.addView(price, pp);

        TextView total = tv("R 0.00", 14, BLACK, true);
        total.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, dp(46), 1);
        tlp.leftMargin = dp(8);
        nums.addView(total, tlp);
        card.addView(nums);
        parent.addView(card, new LinearLayout.LayoutParams(-1, -2));

        descFields.add(desc); qtyFields.add(qty); priceFields.add(price); totalLabels.add(total);

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            public void onTextChanged(CharSequence s, int st, int before, int count) { syncItemsFromFields(); recalculate(); }
            public void afterTextChanged(Editable e) { }
        };
        desc.addTextChangedListener(watcher);
        qty.addTextChangedListener(watcher);
        price.addTextChangedListener(watcher);

        remove.setOnClickListener(v -> {
            int idx = descFields.indexOf(desc);
            if (idx >= 0) {
                items.remove(idx);
                descFields.remove(idx); qtyFields.remove(idx); priceFields.remove(idx); totalLabels.remove(idx);
                parent.removeView(card);
                recalculate();
            }
        });
    }

    void addTotalsSection() {
        content.addView(space(14));
        LinearLayout calc = new LinearLayout(this);
        calc.setOrientation(LinearLayout.VERTICAL);
        calc.setPadding(dp(14), dp(14), dp(14), dp(14));
        calc.setBackgroundResource(R.drawable.bg_card);
        content.addView(calc);

        calc.addView(tv("TOTALS", 16, BLACK, true));
        calc.addView(space(8));
        discountField = input("Discount (R)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        calc.addView(discountField, full(48));
        calc.addView(space(8));

        vatCheck = new CheckBox(this);
        vatCheck.setText("Apply VAT");
        vatCheck.setTextSize(15);
        vatCheck.setChecked(prefs.getBoolean("vat", false));
        calc.addView(vatCheck);

        vatRateField = input("VAT rate %", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        vatRateField.setText(prefs.getString("vatRate", "15"));
        calc.addView(vatRateField, full(48));
        calc.addView(space(10));

        if ("INVOICE".equals(documentType)) {
            calc.addView(tv("PAYMENT STATUS", 14, BLACK, true));
            calc.addView(space(4));
            paymentStatus = new Spinner(this);
            paymentStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"Unpaid", "Partially Paid", "Paid"}));
            paymentStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) { }
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (paidField == null) return;
                    if (position == 0) paidField.setText("0");
                    else if (position == 2 && grandTotalLabel != null) {
                        String t = grandTotalLabel.getText().toString().replace("TOTAL: R ", "").replace(",", "");
                        paidField.setText(t);
                    }
                    recalculate();
                }
            });
            calc.addView(paymentStatus, full(52));
            calc.addView(space(8));
            paidField = input("Amount Paid (R)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            calc.addView(paidField, full(48));
            calc.addView(space(10));
        }

        subtotalLabel = tv("Subtotal: R 0.00", 17, BLACK, true);
        vatLabel = tv("VAT: N/A", 15, Color.DKGRAY, false);
        grandTotalLabel = tv("TOTAL: R 0.00", 21, ORANGE, true);
        balanceLabel = tv("Balance: R 0.00", 15, Color.DKGRAY, true);
        grandTotalLabel.setPadding(0, dp(8), 0, dp(2));
        calc.addView(subtotalLabel);
        calc.addView(vatLabel);
        calc.addView(grandTotalLabel);
        if ("INVOICE".equals(documentType)) calc.addView(balanceLabel);

        TextWatcher t = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { recalculate(); }
            public void afterTextChanged(Editable e) { }
        };
        discountField.addTextChangedListener(t);
        vatRateField.addTextChangedListener(t);
        if (paidField != null) paidField.addTextChangedListener(t);
        vatCheck.setOnCheckedChangeListener((b, checked) -> recalculate());
        recalculate();
    }

    void syncItemsFromFields() {
        for (int i = 0; i < descFields.size() && i < items.size(); i++) {
            Item x = items.get(i);
            x.description = descFields.get(i).getText().toString();
            x.qty = parseDouble(qtyFields.get(i).getText().toString(), 0);
            x.unitPrice = parseDouble(priceFields.get(i).getText().toString(), 0);
        }
    }

    void recalculate() {
        if (totalLabels == null) return;
        syncItemsFromFields();
        double sub = 0;
        for (int i = 0; i < items.size(); i++) {
            double total = items.get(i).qty * items.get(i).unitPrice;
            sub += total;
            if (i < totalLabels.size()) totalLabels.get(i).setText("R " + fmt(total));
        }
        double discount = parseDouble(discountField == null ? "" : discountField.getText().toString(), 0);
        discount = Math.max(0, Math.min(discount, sub));
        boolean apply = vatCheck != null && vatCheck.isChecked();
        double rate = parseDouble(vatRateField == null ? "" : vatRateField.getText().toString(), 15);
        double taxable = Math.max(0, sub - discount);
        double vat = apply ? taxable * rate / 100.0 : 0;
        double grand = taxable + vat;
        double paid = paidField == null ? 0 : Math.max(0, parseDouble(paidField.getText().toString(), 0));
        paid = Math.min(paid, grand);
        double balance = Math.max(0, grand - paid);

        if (subtotalLabel != null) subtotalLabel.setText("Subtotal: R " + fmt(sub));
        if (vatLabel != null) vatLabel.setText(apply ? "VAT @ " + fmt(rate) + "%: R " + fmt(vat) : "VAT: N/A");
        if (grandTotalLabel != null) grandTotalLabel.setText("TOTAL: R " + fmt(grand));
        if (balanceLabel != null) balanceLabel.setText("Balance: R " + fmt(balance));
    }

    double parseDouble(String s, double d) {
        try { return Double.parseDouble(s == null ? "" : s.trim().replace(",", "")); }
        catch (Exception e) { return d; }
    }

    String fmt(double v) { return String.format(Locale.US, "%,.2f", v); }
    String moneyPlain(double v) { return String.format(Locale.US, "%.2f", v); }
    String formatQty(double v) { return v == (long) v ? Long.toString((long) v) : moneyPlain(v); }

    void generatePdfAndShare(boolean share) {
        syncItemsFromFields();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date());
        String number = peekNextNumber(documentType);
        String customer = customerName.getText().toString().trim();
        double sub = 0;
        for (Item i : items) sub += i.qty * i.unitPrice;
        double discount = Math.max(0, Math.min(parseDouble(discountField.getText().toString(), 0), sub));
        boolean apply = vatCheck.isChecked();
        double rate = parseDouble(vatRateField.getText().toString(), 15);
        double vat = apply ? Math.max(0, sub - discount) * rate / 100.0 : 0;
        double total = Math.max(0, sub - discount) + vat;
        String status = paymentStatus == null ? "Unpaid" : paymentStatus.getSelectedItem().toString();
        double paid = paidField == null ? 0 : parseDouble(paidField.getText().toString(), 0);

        PdfGenerator.Doc doc = new PdfGenerator.Doc(
                documentType, number, date, customer,
                customerPhone.getText().toString(), customerEmail.getText().toString(), customerAddress.getText().toString(),
                jobRef.getText().toString(), vehicleReg.getText().toString(), items,
                discount, apply, rate, vat, total, status, paid
        );

        try {
            File out = PdfGenerator.generate(this, doc);
            incrementNumber(documentType);
            saveDocumentHistory(doc, out);
            if (share) shareFile(out); else showGenerated(out, doc);
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("PDF error").setMessage(e.getMessage()).setPositiveButton("OK", null).show();
        }
    }

    String peekNextNumber(String type) {
        String key = "INVOICE".equals(type) ? "invoiceNo" : "quoteNo";
        long n = prefs.getLong(key, 0) + 1;
        return ("INVOICE".equals(type) ? "INV-" : "QT-")
                + new SimpleDateFormat("yyyyMM", Locale.US).format(new Date())
                + String.format(Locale.US, "-%04d", n);
    }

    void incrementNumber(String type) {
        String key = "INVOICE".equals(type) ? "invoiceNo" : "quoteNo";
        prefs.edit().putLong(key, prefs.getLong(key, 0) + 1).apply();
    }

    String nextNumber(String type) {
        String n = peekNextNumber(type);
        incrementNumber(type);
        return n;
    }

    void saveDocumentHistory(PdfGenerator.Doc doc, File f) {
        try {
            JSONArray a = new JSONArray(prefs.getString("history", "[]"));
            JSONObject o = docToJson(doc);
            o.put("path", f.getAbsolutePath());
            o.put("createdAt", System.currentTimeMillis());
            a.put(o);
            prefs.edit().putString("history", a.toString()).apply();
            saveCustomer();
        } catch (Exception ignored) { }
    }

    JSONObject docToJson(PdfGenerator.Doc d) throws Exception {
        JSONObject o = new JSONObject();
        o.put("type", d.type); o.put("number", d.number); o.put("date", d.date);
        o.put("customer", d.customer); o.put("phone", d.phone); o.put("email", d.email); o.put("address", d.address);
        o.put("jobRef", d.jobRef); o.put("vehicleReg", d.vehicleReg);
        o.put("discount", d.discount); o.put("applyVat", d.applyVat); o.put("rate", d.rate); o.put("vat", d.vat); o.put("total", d.total);
        o.put("paymentStatus", d.paymentStatus); o.put("paid", d.paid);
        JSONArray ia = new JSONArray();
        for (Item i : d.items) {
            JSONObject io = new JSONObject(); io.put("description", i.description); io.put("qty", i.qty); io.put("unitPrice", i.unitPrice); ia.put(io);
        }
        o.put("items", ia);
        return o;
    }

    void saveCustomer() {
        try {
            String name = customerName.getText().toString().trim();
            if (name.isEmpty()) return;
            JSONArray a = getCustomersArray();
            JSONObject o = new JSONObject();
            o.put("name", name); o.put("phone", customerPhone.getText().toString());
            o.put("email", customerEmail.getText().toString()); o.put("address", customerAddress.getText().toString());
            boolean replaced = false;
            for (int i = 0; i < a.length(); i++) {
                if (a.getJSONObject(i).optString("name").equalsIgnoreCase(name)) { a.put(i, o); replaced = true; break; }
            }
            if (!replaced) a.put(o);
            prefs.edit().putString("customers", a.toString()).apply();
        } catch (Exception ignored) { }
    }

    JSONArray getCustomersArray() {
        try { return new JSONArray(prefs.getString("customers", "[]")); }
        catch (Exception e) { return new JSONArray(); }
    }

    void showGenerated(File out, PdfGenerator.Doc doc) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(doc.type + " created")
                .setMessage(doc.number + "\nTotal: R " + fmt(doc.total) + "\n\nSaved to the app and Downloads.")
                .setPositiveButton("Share", (d, w) -> shareFile(out))
                .setNeutralButton("Save As", (d, w) -> savePdfAs(out))
                .setNegativeButton("Open PDF", (d, w) -> openPdf(out))
                .create();
        dialog.show();
    }

    File pendingPdfSave;

    void savePdfAs(File file) {
        pendingPdfSave = file;
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("application/pdf");
        i.putExtra(Intent.EXTRA_TITLE, file.getName());
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, 43);
    }

    void openPdf(File file) {
        try {
            Uri uri = PdfProvider.uriFor(this, file);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, "application/pdf");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer is available. The file was still saved.", Toast.LENGTH_LONG).show();
        }
    }

    void shareFile(File file) {
        try {
            Uri uri = PdfProvider.uriFor(this, file);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("application/pdf");
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(i, "Send 93 Services PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to share PDF", Toast.LENGTH_SHORT).show();
        }
    }

    void showCustomers() {
        setupRoot(true);
        content.addView(tv("CUSTOMERS", 24, BLACK, true));
        content.addView(space(10));
        JSONArray a = getCustomersArray();
        if (a.length() == 0) {
            content.addView(tv("No saved customers yet.", 15, Color.DKGRAY, false));
            return;
        }
        for (int i = a.length() - 1; i >= 0; i--) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) continue;
            LinearLayout c = new LinearLayout(this);
            c.setOrientation(LinearLayout.VERTICAL);
            c.setPadding(dp(14), dp(12), dp(14), dp(12));
            c.setBackgroundResource(R.drawable.bg_card);
            c.addView(tv(o.optString("name"), 17, BLACK, true));
            c.addView(tv(o.optString("phone"), 14, Color.DKGRAY, false));
            c.addView(tv(o.optString("email"), 14, Color.DKGRAY, false));
            c.addView(tv(o.optString("address"), 13, Color.DKGRAY, false));
            content.addView(c);
            content.addView(space(10));
        }
    }

    void showHistory() {
        setupRoot(true);
        content.addView(tv("DOCUMENT HISTORY", 24, BLACK, true));
        content.addView(space(10));
        JSONArray a;
        try { a = new JSONArray(prefs.getString("history", "[]")); }
        catch (Exception e) { a = new JSONArray(); }
        if (a.length() == 0) {
            content.addView(tv("No documents created yet.", 15, Color.DKGRAY, false));
            return;
        }
        for (int i = a.length() - 1; i >= 0; i--) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) continue;
            LinearLayout c = new LinearLayout(this);
            c.setOrientation(LinearLayout.VERTICAL);
            c.setPadding(dp(14), dp(12), dp(14), dp(12));
            c.setBackgroundResource(R.drawable.bg_card);
            c.addView(tv(o.optString("type") + "  " + o.optString("number"), 16, BLACK, true));
            c.addView(tv(o.optString("customer", "No customer") + "  •  " + o.optString("date"), 14, Color.DKGRAY, false));
            c.addView(tv("R " + fmt(o.optDouble("total", 0)), 15, ORANGE, true));
            c.addView(tv("Status: " + o.optString("paymentStatus", "Unpaid"), 13, Color.DKGRAY, false));

            LinearLayout actions = row();
            Button open = btn("Open PDF", false);
            open.setOnClickListener(v -> {
                File f = new File(o.optString("path"));
                if (f.exists()) openPdf(f);
                else regenerateHistoryPdf(o, true);
            });
            actions.addView(open, new LinearLayout.LayoutParams(0, dp(48), 1));

            Button share = btn("Share PDF", false);
            share.setOnClickListener(v -> {
                File f = new File(o.optString("path"));
                if (f.exists()) shareFile(f);
                else regenerateHistoryPdf(o, true);
            });
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, dp(48), 1);
            sp.leftMargin = dp(8);
            actions.addView(share, sp);

            Button reuse = btn("Use Again", false);
            reuse.setOnClickListener(v -> loadHistoryAsNew(o));
            LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(0, dp(48), 1);
            up.leftMargin = dp(8);
            actions.addView(reuse, up);
            c.addView(actions);

            if ("QUOTE".equalsIgnoreCase(o.optString("type"))) {
                Button convert = btn("Convert to Invoice", true);
                convert.setOnClickListener(v -> loadHistoryAsNew(o));
                c.addView(space(8));
                c.addView(convert, full(50));
            }

            content.addView(c);
            content.addView(space(10));
        }
    }

    void loadHistoryAsNew(JSONObject o) {
        try {
            documentType = "QUOTE".equalsIgnoreCase(o.optString("type")) ? "QUOTE" : "INVOICE";
            if ("QUOTE".equalsIgnoreCase(o.optString("type"))) {
                new AlertDialog.Builder(this)
                        .setTitle("Use this quote as a new invoice?")
                        .setMessage("This will load the quote details into a new invoice. The original quote stays unchanged.")
                        .setPositiveButton("Create Invoice", (d, w) -> {
                            documentType = "INVOICE";
                            loadDocFields(o);
                            showEditor();
                        })
                        .setNegativeButton("Use as Quote", (d, w) -> {
                            documentType = "QUOTE";
                            loadDocFields(o);
                            showEditor();
                        }).show();
            } else {
                loadDocFields(o);
                showEditor();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not load document.", Toast.LENGTH_SHORT).show();
        }
    }

void loadDocFields(JSONObject o) {
    try {
        items.clear();

        JSONArray ia = o.optJSONArray("items");
        if (ia != null) {
            for (int i = 0; i < ia.length(); i++) {
                JSONObject x = ia.getJSONObject(i);
                items.add(new Item(
                        x.optString("description"),
                        x.optDouble("qty", 1),
                        x.optDouble("unitPrice", 0)
                ));
            }
        }

        String customer = o.optString("customer");
        String phone = o.optString("phone");
        String email = o.optString("email");
        String address = o.optString("address");
        String ref = o.optString("jobRef");
        String reg = o.optString("vehicleReg");

        prefs.edit()
                .putString("edit_customer", customer)
                .putString("edit_phone", phone)
                .putString("edit_email", email)
                .putString("edit_address", address)
                .putString("edit_jobRef", ref)
                .putString("edit_vehicleReg", reg)
                .putString("edit_discount", Double.toString(o.optDouble("discount", 0)))
                .putBoolean("edit_applyVat", o.optBoolean("applyVat", false))
                .putString("edit_vatRate", Double.toString(o.optDouble("rate", 15)))
                .putString("edit_paid", Double.toString(o.optDouble("paid", 0)))
                .putString("edit_paymentStatus", o.optString("paymentStatus", "Unpaid"))
                .apply();

    } catch (Exception e) {
        Toast.makeText(
                this,
                "Could not load document: " + e.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }
}

        // Fields are created by showEditor; keep the values temporarily in preferences for this editing session.
        prefs.edit()
                .putString("edit_customer", customer).putString("edit_phone", phone)
                .putString("edit_email", email).putString("edit_address", address)
                .putString("edit_jobRef", ref).putString("edit_vehicleReg", reg)
                .putString("edit_discount", Double.toString(o.optDouble("discount", 0)))
                .putBoolean("edit_applyVat", o.optBoolean("applyVat", false))
                .putString("edit_vatRate", Double.toString(o.optDouble("rate", 15)))
                .putString("edit_paid", Double.toString(o.optDouble("paid", 0)))
                .putString("edit_paymentStatus", o.optString("paymentStatus", "Unpaid"))
                .apply();
    }

    void applyPendingEditValues() {
        if (customerName == null) return;
        customerName.setText(prefs.getString("edit_customer", ""));
        customerPhone.setText(prefs.getString("edit_phone", ""));
        customerEmail.setText(prefs.getString("edit_email", ""));
        customerAddress.setText(prefs.getString("edit_address", ""));
        jobRef.setText(prefs.getString("edit_jobRef", ""));
        vehicleReg.setText(prefs.getString("edit_vehicleReg", ""));
        if (discountField != null) discountField.setText(prefs.getString("edit_discount", ""));
        if (vatCheck != null) vatCheck.setChecked(prefs.getBoolean("edit_applyVat", false));
        if (vatRateField != null) vatRateField.setText(prefs.getString("edit_vatRate", "15"));
        if (paidField != null) paidField.setText(prefs.getString("edit_paid", ""));
        if (paymentStatus != null) {
            String wanted = prefs.getString("edit_paymentStatus", "Unpaid");
            for (int i = 0; i < paymentStatus.getCount(); i++) if (wanted.equals(paymentStatus.getItemAtPosition(i))) { paymentStatus.setSelection(i); break; }
        }
        prefs.edit().remove("edit_customer").remove("edit_phone").remove("edit_email").remove("edit_address")
                .remove("edit_jobRef").remove("edit_vehicleReg").remove("edit_discount").remove("edit_applyVat")
                .remove("edit_vatRate").remove("edit_paid").remove("edit_paymentStatus").apply();
    }

    void regenerateHistoryPdf(JSONObject o, boolean share) {
        try {
            PdfGenerator.Doc d = jsonToDoc(o);
            File f = PdfGenerator.generate(this, d);
            o.put("path", f.getAbsolutePath());
            JSONArray a = new JSONArray(prefs.getString("history", "[]"));
            for (int i = 0; i < a.length(); i++) {
                if (d.number.equals(a.getJSONObject(i).optString("number"))) { a.put(i, o); break; }
            }
            prefs.edit().putString("history", a.toString()).apply();
            if (share) shareFile(f); else openPdf(f);
        } catch (Exception e) {
            Toast.makeText(this, "Could not regenerate PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    PdfGenerator.Doc jsonToDoc(JSONObject o) throws Exception {
        ArrayList<Item> list = new ArrayList<>();
        JSONArray ia = o.optJSONArray("items");
        if (ia != null) {
            for (int i = 0; i < ia.length(); i++) {
                JSONObject x = ia.getJSONObject(i);
                list.add(new Item(x.optString("description"), x.optDouble("qty", 1), x.optDouble("unitPrice", 0)));
            }
        }
        return new PdfGenerator.Doc(
                o.optString("type", "INVOICE"), o.optString("number"), o.optString("date"),
                o.optString("customer"), o.optString("phone"), o.optString("email"), o.optString("address"),
                o.optString("jobRef"), o.optString("vehicleReg"), list, o.optDouble("discount", 0),
                o.optBoolean("applyVat", false), o.optDouble("rate", 15), o.optDouble("vat", 0),
                o.optDouble("total", 0), o.optString("paymentStatus", "Unpaid"), o.optDouble("paid", 0));
    }

    void showServiceSettings() {
        setupRoot(true);
        content.addView(tv("SERVICES & PRICES", 24, BLACK, true));
        content.addView(space(8));
        content.addView(tv("Edit default services and unit prices. Everything is stored on this device and works offline.", 14, Color.DKGRAY, false));
        content.addView(space(12));
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list);
        for (String s : new ArrayList<>(defaultServices)) addServiceSettingRow(list, s);

        content.addView(space(10));
        Button add = btn("+ Add Service", true);
        add.setOnClickListener(v -> addServiceDialog());
        content.addView(add, full(54));
        content.addView(space(10));
        Button vat = btn("VAT Settings", false);
        vat.setOnClickListener(v -> vatSettingsDialog());
        content.addView(vat, full(54));
    }

    void addServiceSettingRow(LinearLayout parent, String name) {
        LinearLayout r = row();
        r.setPadding(dp(12), dp(8), dp(12), dp(8));
        r.setBackgroundResource(R.drawable.bg_card);
        TextView n = tv(name + "\nDefault: R " + fmt(servicePrices.getOrDefault(name, 0.0)), 14, BLACK, true);
        r.addView(n, new LinearLayout.LayoutParams(0, dp(60), 1));
        Button edit = btn("Edit", false);
        edit.setOnClickListener(v -> editService(name));
        r.addView(edit, new LinearLayout.LayoutParams(dp(82), dp(48)));
        parent.addView(r);
        parent.addView(space(8));
    }

    void editService(String name) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), 0, dp(24), 0);
        EditText n = input("Service name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        n.setText(name);
        EditText p = input("Default unit price", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        p.setText(moneyPlain(servicePrices.getOrDefault(name, 0.0)));
        box.addView(n, full(48)); box.addView(space(8)); box.addView(p, full(48));
        new AlertDialog.Builder(this).setTitle("Edit Service").setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = n.getText().toString().trim();
                    if (newName.isEmpty()) return;
                    for (String existing : defaultServices) {
                        if (!existing.equals(name) && existing.equalsIgnoreCase(newName)) {
                            Toast.makeText(this, "A service with that name already exists.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    defaultServices.remove(name);
                    servicePrices.remove(name);
                    defaultServices.add(newName);
                    servicePrices.put(newName, parseDouble(p.getText().toString(), 0));
                    saveServices(); showServiceSettings();
                })
                .setNegativeButton("Delete", (d, w) -> {
                    defaultServices.remove(name); servicePrices.remove(name); saveServices(); showServiceSettings();
                })
                .setNeutralButton("Cancel", null).show();
    }

    void addServiceDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), 0, dp(24), 0);
        EditText n = input("Service name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        EditText p = input("Default unit price", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        box.addView(n, full(48)); box.addView(space(8)); box.addView(p, full(48));
        new AlertDialog.Builder(this).setTitle("Add Service").setView(box)
                .setPositiveButton("Add", (d, w) -> {
                    String s = n.getText().toString().trim();
                    if (!s.isEmpty() && !defaultServices.contains(s)) {
                        defaultServices.add(s); servicePrices.put(s, parseDouble(p.getText().toString(), 0)); saveServices(); showServiceSettings();
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    void vatSettingsDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), 0, dp(24), 0);
        CheckBox enabled = new CheckBox(this);
        enabled.setText("Apply VAT by default");
        enabled.setChecked(prefs.getBoolean("vat", false));
        EditText rate = input("VAT rate %", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        rate.setText(prefs.getString("vatRate", "15"));
        box.addView(enabled); box.addView(space(8)); box.addView(rate, full(48));
        new AlertDialog.Builder(this).setTitle("VAT Settings").setView(box)
                .setPositiveButton("Save", (d, w) -> prefs.edit().putBoolean("vat", enabled.isChecked()).putString("vatRate", rate.getText().toString().trim()).apply())
                .setNegativeButton("Cancel", null).show();
    }

    void showBackup() {
        setupRoot(true);
        content.addView(tv("BACKUP / RESTORE", 24, BLACK, true));
        content.addView(space(8));
        content.addView(tv("All 93 Services information is local to this device. Export a backup before replacing the phone.", 14, Color.DKGRAY, false));
        content.addView(space(16));
        Button ex = btn("Export Backup File", true);
        ex.setOnClickListener(v -> exportBackup());
        content.addView(ex, full(56));
        content.addView(space(10));
        Button im = btn("Restore Backup File", false);
        im.setOnClickListener(v -> restoreBackup());
        content.addView(im, full(56));
        content.addView(space(14));
        content.addView(tv("Backup includes services/prices, customers, document history and numbering.\n\nDesigned & Developed by TECHART (PTY) LTD", 13, Color.DKGRAY, false));
    }

    void exportBackup() {
        try {
            File f = backupFile();
            JSONObject root = new JSONObject();
            root.put("backupVersion", 2);
            root.put("generatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            root.put("services", new JSONArray(prefs.getString("services", "[]")));
            root.put("customers", new JSONArray(prefs.getString("customers", "[]")));
            root.put("history", new JSONArray(prefs.getString("history", "[]")));
            root.put("invoiceNo", prefs.getLong("invoiceNo", 0));
            root.put("quoteNo", prefs.getLong("quoteNo", 0));
            root.put("vat", prefs.getBoolean("vat", false));
            root.put("vatRate", prefs.getString("vatRate", "15"));
            try (FileWriter w = new FileWriter(f)) { w.write(root.toString(2)); }
            showBackupShare(f);
        } catch (Exception e) {
            Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    File backupFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir != null && !dir.exists()) dir.mkdirs();
        return new File(dir, "93_services_backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".json");
    }

    void showBackupShare(File f) {
        new AlertDialog.Builder(this).setTitle("Backup created").setMessage(f.getName())
                .setPositiveButton("Share", (d, w) -> shareGeneric(f, "application/json"))
                .setNegativeButton("Close", null).show();
    }

    void shareGeneric(File f, String type) {
        try {
            Uri uri = PdfProvider.uriFor(this, f);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType(type); i.putExtra(Intent.EXTRA_STREAM, uri); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(i, "Send backup"));
        } catch (Exception e) { Toast.makeText(this, "Unable to share backup", Toast.LENGTH_SHORT).show(); }
    }

    void restoreBackup() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/json");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, 42);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 43 && res == RESULT_OK && data != null && pendingPdfSave != null) {
            try {
                Uri target = data.getData();
                try (InputStream in = new FileInputStream(pendingPdfSave); OutputStream out = getContentResolver().openOutputStream(target)) {
                    byte[] b = new byte[8192]; int n;
                    while ((n = in.read(b)) > 0) out.write(b, 0, n);
                }
                Toast.makeText(this, "PDF saved.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Could not save PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                pendingPdfSave = null;
            }
            return;
        }
        if (req == 42 && res == RESULT_OK && data != null) {
            try {
                String json = readUri(data.getData());
                JSONObject o = new JSONObject(json);
                if (o.optJSONArray("services") == null || o.optJSONArray("customers") == null || o.optJSONArray("history") == null) {
                    throw new IllegalArgumentException("This is not a valid 93 Services backup file.");
                }
                prefs.edit()
                        .putString("services", o.optJSONArray("services").toString())
                        .putString("customers", o.optJSONArray("customers").toString())
                        .putString("history", o.optJSONArray("history").toString())
                        .putLong("invoiceNo", o.optLong("invoiceNo", 0))
                        .putLong("quoteNo", o.optLong("quoteNo", 0))
                        .putBoolean("vat", o.optBoolean("vat", false))
                        .putString("vatRate", o.optString("vatRate", "15"))
                        .apply();
                loadServices();
                new AlertDialog.Builder(this).setTitle("Restore complete")
                        .setMessage("93 Services data has been restored to this phone.")
                        .setPositiveButton("OK", null).show();
            } catch (Exception e) {
                new AlertDialog.Builder(this).setTitle("Restore failed").setMessage(e.getMessage()).setPositiveButton("OK", null).show();
            }
        }
    }

    String readUri(Uri uri) throws Exception {
        InputStream in = getContentResolver().openInputStream(uri);
        StringBuilder s = new StringBuilder();
        byte[] b = new byte[4096];
        int n;
        while ((n = in.read(b)) > 0) s.append(new String(b, 0, n));
        in.close();
        return s.toString();
    }

    void loadServices() {
        defaultServices.clear(); servicePrices.clear();
        String saved = prefs.getString("services", null);
        try {
            if (saved != null) {
                JSONArray a = new JSONArray(saved);
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    String n = o.optString("name");
                    if (!n.isEmpty()) { defaultServices.add(n); servicePrices.put(n, o.optDouble("price", 0)); }
                }
                return;
            }
        } catch (Exception ignored) { }

        String[] seed = {
                "Towing", "Bou Sand", "River Sand", "Red Soil", "G5 Gravel",
                "Supply Fire Extinguisher", "Install Fire Extinguisher", "Supply Hose Reel",
                "Security Guard", "Roofing", "Steel Work", "Aluminium & Glass", "Ceiling",
                "Servicing / Maintenance"
        };
        for (String s : seed) { defaultServices.add(s); servicePrices.put(s, 0.0); }
        servicePrices.put("Bou Sand", 2500.0);
        saveServices();
    }

    void saveServices() {
        try {
            JSONArray a = new JSONArray();
            for (String s : defaultServices) {
                JSONObject o = new JSONObject(); o.put("name", s); o.put("price", servicePrices.getOrDefault(s, 0.0)); a.put(o);
            }
            prefs.edit().putString("services", a.toString()).apply();
        } catch (Exception ignored) { }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static class Item {
        public String description;
        public double qty;
        public double unitPrice;
        public Item(String d, double q, double p) { description = d; qty = q; unitPrice = p; }
    }
}
