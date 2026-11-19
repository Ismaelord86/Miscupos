package cl.miscupos.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.NumberFormat;
import java.util.*;

public class MainActivity extends Activity {
  final int BG=0xff090e27, PANEL=0xff172044, WHITE=0xfff5f7ff, MUTED=0xffa9b6d6, CYAN=0xff16e5f3, PURPLE=0xffae44ff, GREEN=0xff39efbd;
  LinearLayout root, list, summary; TextView status; String scope="Todas"; JSONArray cards;
  final String[] banks={"BCI","Itaú","Banco de Chile","BancoEstado","Scotiabank","Tenpo","Mercado Pago"};
  int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  String money(long n){return "$"+NumberFormat.getIntegerInstance(new Locale("es","CL")).format(n);}
  GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  GradientDrawable neon(){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xff008be8,0xffa42dff,0xffef3bc7});d.setCornerRadius(dp(20));return d;}
  TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
  LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  void pad(View v,int x,int y){v.setPadding(dp(x),dp(y),dp(x),dp(y));}
  void space(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
  void save(){getPreferences(0).edit().putString("cards",cards.toString()).apply();}
  JSONArray defaults(){JSONArray a=new JSONArray();addDefault(a,"BCI","Visa Signature","Crédito","Personal",3000000,1800000);addDefault(a,"Itaú","Mastercard Black","Crédito","Personal",4000000,2100000);addDefault(a,"Banco de Chile","Visa","Crédito","Empresa",5000000,3500000);addDefault(a,"BancoEstado","Cuenta corriente","Débito","Empresa",0,1250000);addDefault(a,"Scotiabank","Mastercard","Crédito","Empresa",2000000,900000);addDefault(a,"Tenpo","Cuenta digital","Débito","Personal",0,185000);addDefault(a,"Mercado Pago","Cuenta digital","Débito","Empresa",0,420000);return a;}
  void addDefault(JSONArray a,String b,String n,String t,String s,long total,long available){JSONObject o=new JSONObject();try{o.put("bank",b);o.put("name",n);o.put("type",t);o.put("scope",s);o.put("total",total);o.put("available",available);}catch(Exception ignored){}a.put(o);}
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);try{cards=new JSONArray(getPreferences(0).getString("cards",""));}catch(Exception e){cards=defaults();save();}render();}
  TextView button(String label,boolean bright){TextView t=text(label,15,WHITE,true);t.setGravity(Gravity.CENTER);pad(t,14,14);t.setBackground(bright?neon():bg(PANEL,16));return t;}
  void render(){
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);root=column();pad(root,18,22);scroll.addView(root);setContentView(scroll);
    TextView heading=text("◈  Mis Cupos",29,WHITE,true);root.addView(heading);root.addView(text("Tus tarjetas, en un solo lugar",13,MUTED,false));space(root,24);
    LinearLayout tabs=new LinearLayout(this);tabs.setOrientation(0);for(String s:new String[]{"Todas","Personal","Empresa"}){TextView t=button(s,s.equals(scope));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(dp(2),0,dp(2),0);tabs.addView(t,p);t.setOnClickListener(v->{scope=s;render();});}root.addView(tabs);space(root,22);
    summary=column();root.addView(summary);space(root,20);
    LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);TextView title=text("Mis tarjetas",21,WHITE,true);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));TextView add=button("+ Agregar",true);header.addView(add);add.setOnClickListener(v->edit(-1));root.addView(header);space(root,12);
    list=column();root.addView(list);space(root,20);
    TextView refresh=button("⟳  ACTUALIZAR TODAS",true);root.addView(refresh);refresh.setOnClickListener(v->{getPreferences(0).edit().putLong("updated",System.currentTimeMillis()).apply();new AlertDialog.Builder(this).setTitle("Modo demostración").setMessage("Se revisaron las tarjetas de prueba. No se consultaron bancos reales ni se modificaron sus importes.").setPositiveButton("Entendido",null).show();render();});space(root,10);
    status=text("Sin conexión bancaria real · Última simulación: "+lastUpdate(),12,MUTED,false);status.setGravity(Gravity.CENTER);root.addView(status);space(root,14);
    TextView note=text("DEMO · Importes ficticios guardados solo en este dispositivo. El crédito disponible no es dinero en cuenta.",11,MUTED,false);root.addView(note);
    populate();
  }
  String lastUpdate(){long t=getPreferences(0).getLong("updated",0);return t==0?"Nunca":new java.text.SimpleDateFormat("dd/MM HH:mm",new Locale("es","CL")).format(new Date(t));}
  void populate(){long credit=0,debit=0,limit=0;int count=0;for(int i=0;i<cards.length();i++){JSONObject o=cards.optJSONObject(i);if(o==null||!matches(o))continue;count++;long a=o.optLong("available");if("Crédito".equals(o.optString("type"))){credit+=a;limit+=o.optLong("total");}else debit+=a;}
    LinearLayout hero=column();pad(hero,18,18);hero.setBackground(bg(PANEL,20));hero.addView(text("CRÉDITO DISPONIBLE",13,CYAN,true));space(hero,8);hero.addView(text(money(credit),30,WHITE,true));hero.addView(text("Límite total: "+money(limit),12,MUTED,false));space(hero,15);hero.addView(text("SALDO EN DÉBITO",13,GREEN,true));space(hero,6);hero.addView(text(money(debit),25,WHITE,true));summary.addView(hero);
    for(int i=0;i<cards.length();i++){final int index=i;JSONObject o=cards.optJSONObject(i);if(o==null||!matches(o))continue;LinearLayout card=column();pad(card,16,15);card.setBackground(bg(PANEL,18));TextView name=text(o.optString("bank")+"  ·  "+o.optString("name"),16,WHITE,true);card.addView(name);space(card,5);card.addView(text(o.optString("type")+" · "+o.optString("scope"),12,MUTED,false));space(card,8);card.addView(text(money(o.optLong("available"))+" disponible",20,CYAN,true));if("Crédito".equals(o.optString("type"))){long total=o.optLong("total");card.addView(text("Cupo total "+money(total)+"  ·  Utilizado "+money(Math.max(0,total-o.optLong("available"))),11,MUTED,false));ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);progress.setProgress(total>0?(int)Math.min(100,o.optLong("available")*100/total):0);progress.setProgressTintList(android.content.res.ColorStateList.valueOf(CYAN));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(6));pp.topMargin=dp(9);card.addView(progress,pp);}LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.bottomMargin=dp(10);list.addView(card,cp);card.setOnClickListener(v->edit(index));}
    if(count==0)list.addView(text("No hay tarjetas en esta categoría.",14,MUTED,false));
  }
  boolean matches(JSONObject o){return scope.equals("Todas")||scope.equals(o.optString("scope"));}
  EditText field(String hint,String value,boolean numeric){EditText e=new EditText(this);e.setSingleLine(true);e.setTextColor(WHITE);e.setHintTextColor(MUTED);e.setTextSize(15);e.setHint(hint);e.setText(value);if(numeric)e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);return e;}
  void edit(int index){JSONObject original=index>=0?cards.optJSONObject(index):null;LinearLayout form=column();pad(form,18,6);Spinner bank=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,banks);bank.setAdapter(adapter);String oldBank=original==null?banks[0]:original.optString("bank");for(int i=0;i<banks.length;i++)if(banks[i].equals(oldBank))bank.setSelection(i);form.addView(text("Banco",13,MUTED,false));form.addView(bank);EditText name=field("Nombre de la tarjeta / cuenta",original==null?"":original.optString("name"),false);form.addView(name);
    Spinner type=new Spinner(this);type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Crédito","Débito"}));type.setSelection(original!=null&&"Débito".equals(original.optString("type"))?1:0);form.addView(text("Tipo",13,MUTED,false));form.addView(type);
    Spinner owner=new Spinner(this);owner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Personal","Empresa"}));owner.setSelection(original!=null&&"Empresa".equals(original.optString("scope"))?1:0);form.addView(text("Titular",13,MUTED,false));form.addView(owner);
    EditText total=field("Cupo total (0 si es débito)",original==null?"":String.valueOf(original.optLong("total")),true);form.addView(total);EditText available=field("Cupo o saldo disponible",original==null?"":String.valueOf(original.optLong("available")),true);form.addView(available);
    AlertDialog.Builder dialog=new AlertDialog.Builder(this).setTitle(index<0?"Agregar tarjeta":"Editar tarjeta").setView(form).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",null);if(index>=0)dialog.setNeutralButton("Eliminar",(d,w)->new AlertDialog.Builder(this).setMessage("¿Eliminar esta tarjeta?").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(dd,ww)->{JSONArray next=new JSONArray();for(int i=0;i<cards.length();i++)if(i!=index)next.put(cards.opt(i));cards=next;save();render();}).show());AlertDialog a=dialog.create();a.setOnShowListener(d->a.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{long t=Long.parseLong(total.getText().toString().trim());long av=Long.parseLong(available.getText().toString().trim());if(t<0||av<0||("Crédito".equals(type.getSelectedItem())&&av>t)){Toast.makeText(this,"Revisa los importes: deben ser positivos y el disponible no puede superar el cupo",Toast.LENGTH_LONG).show();return;}JSONObject o=new JSONObject();o.put("bank",bank.getSelectedItem().toString());o.put("name",name.getText().toString().trim());o.put("type",type.getSelectedItem().toString());o.put("scope",owner.getSelectedItem().toString());o.put("total",t);o.put("available",av);if(o.optString("name").isEmpty()){name.setError("Escribe un nombre");return;}if(index<0)cards.put(o);else cards.put(index,o);save();a.dismiss();render();}catch(Exception ex){Toast.makeText(this,"Ingresa importes numéricos válidos",Toast.LENGTH_LONG).show();}}));a.show();
  }
}
