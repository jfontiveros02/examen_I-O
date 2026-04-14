# Examen Práctico — Sistema de Gestión de Residuos Radiactivos

**Módulo:** Programación (0485) — DAW Bilingüe
**Duración:** 2 horas | **Lenguaje:** Java | **Curso:** 2025/2026

---

## El problema

Has sido contratado como programador en una empresa que gestiona residuos radiactivos. Estos residuos son peligrosos y generan calor, por lo que deben mantenerse refrigerados hasta que su nivel de radiactividad baje lo suficiente como para ser seguros.

Tu trabajo es crear un programa Java que, a partir de unos datos de entrada, calcule automáticamente cuándo será seguro cada residuo y cuánto costará refrigerarlo hasta ese momento.

### ¿Qué es la «actividad» de un residuo?

La **actividad** mide cuántas desintegraciones por segundo se producen en el material radiactivo. Piénsalo como la «temperatura de peligrosidad»: cuanto mayor es el número, más peligroso es el residuo. Con el tiempo ese número **baja de forma automática** siguiendo siempre la misma curva matemática (una exponencial decreciente). Tu programa no necesita saber física: solo aplica las fórmulas que te damos.

> **Dato clave:** el físico de la empresa ya programó las clases `Utilidades` y `FuncionUnivariable` con toda la física. Tú solo tienes que llamar a sus métodos con los parámetros correctos.

---

## Lo que ya tienes hecho

### Interfaz `FuncionUnivariable`

Representa una función matemática de un solo parámetro. Ya está implementada; no la toques. Contiene un único método:

```java
double evaluar(double t);
```

Gracias a `@FunctionalInterface`, puedes usarla con **lambdas** de Java. Por ejemplo: `t -> t * t` representa f(t) = t².

### Clase `Utilidades`

Ya está implementada. No la toques, solo úsala. Contiene:

- **`HashMap<String, Double> energias`**
  Dado el nombre de un isótopo devuelve la energía liberada por cada desintegración en Julios.
  Ejemplo: `Utilidades.energias.get("Co60")`

- **`HashMap<String, Double> semividas`**
  Dado el isótopo devuelve su semivida en segundos (tiempo en que pierde la mitad de su actividad).
  Ejemplo: `Utilidades.semividas.get("Sr90")`

- **`double integrar(FuncionUnivariable f, double limInf, double limSup)`**
  Calcula el área bajo una curva entre dos puntos. Recibe una lambda de Java y los límites de integración.

- **`double biseccion(FuncionUnivariable f, double y, double limInf, double limSup)`**
  Dado un valor objetivo `y`, encuentra el valor de `x` en [limInf, limSup] tal que `f(x) = y`.

Los isótopos disponibles son:

| Clave        | Nombre        | Semivida   |
|--------------|---------------|------------|
| `"Sr90"`     | Estroncio-90  | 28,8 años  |
| `"Co60"`     | Cobalto-60    | 5,27 años  |
| `"Cs137"`    | Cesio-137     | 30,17 años |

---

## Ficheros de entrada y salida

El programa recibe `residuos.csv` y genera `factura.txt`.

### `residuos.csv` — lo que recibes

```
ID;ISOTOPO;ACTIVIDAD_ESPECIFICA;MASA
1;Sr90;8000000000000;500
2;Co60;20000000000000;75
3;Cs137;15000000000000;130
```

`ACTIVIDAD_ESPECIFICA` es la actividad por kilo (Bq/kg). `MASA` es la cantidad en kg. Multiplicando ambas obtienes la actividad total inicial del residuo.

### `factura.txt` — lo que debes generar

Para cada residuo, un bloque con este formato:

```
========================================
FACTURA DE REFRIGERACIÓN — RESIDUO #1
========================================
Isótopo           : Sr90
Masa              : 500.0 kg
Fecha de entrega  : 2025-04-14T09:00
Fecha segura      : 2120-12-14T09:23
Coste total       : 28672.54 €
----------------------------------------
```

---

## Tarea 1 — Clase `Radionuclido` (atributos y getters)

Crea la clase `Radionuclido`. Representa un residuo radiactivo concreto. Cada objeto guarda los datos de un residuo y es capaz de realizar los cálculos sobre él.

### Atributos privados

| Tipo Java       | Nombre                       | Qué representa                                  |
|-----------------|------------------------------|-------------------------------------------------|
| `String`        | `id`                         | Identificador del residuo (viene del CSV)       |
| `String`        | `isotopo`                    | Nombre del isótopo (`"Sr90"`, `"Co60"`…)        |
| `Double`        | `masa`                       | Cantidad de material en kilogramos              |
| `Double`        | `actividadEspecificaInicial` | Actividad por kilo en el momento de entrega (Bq/kg) |
| `LocalDateTime` | `fechaEntrega`               | Fecha y hora en que llega el residuo al centro  |

### Getters

Crea un getter por atributo (`getId()`, `getIsotopo()`, `getMasa()`, etc.).

Además, crea `getActividadInicial()` que **no tiene atributo directo**, sino que calcula y devuelve:

```
getActividadInicial() = actividadEspecificaInicial × masa
```

> Ejemplo: 75 kg de Co60 con 20.000.000.000.000 Bq/kg → actividad inicial de 1.500.000.000.000.000 Bq.

---

## Tarea 2 — Método `actividad(LocalDateTime fecha)`

Responde a: *«¿Cuánta actividad tiene este residuo en una fecha concreta?»*

La fórmula es:

```
A(t) = A0 × e^(-lambda × t)
```

| Símbolo  | Significado                              | Cómo lo calculas en Java                                         |
|----------|------------------------------------------|------------------------------------------------------------------|
| `A(t)`   | Actividad en la fecha indicada           | Es el resultado del método                                       |
| `A0`     | Actividad total inicial                  | `this.getActividadInicial()`                                     |
| `e^(…)`  | Función exponencial                      | `Math.exp( … )`                                                  |
| `lambda` | Constante de desintegración del isótopo  | `Math.log(2) / semivida` donde `semivida = Utilidades.semividas.get(this.isotopo)` |
| `t`      | **Segundos transcurridos** desde `fechaEntrega` hasta `fecha` | `ChronoUnit.SECONDS.between(this.fechaEntrega, fecha)` |

> ⚠️ **Importante:** `t` no es una fecha, es un **número de segundos** (un `long`). La fórmula exponencial trabaja siempre con segundos transcurridos desde el momento de entrega (t=0). Cuando `fecha` coincide con `fechaEntrega`, t=0 y la fórmula devuelve exactamente A0.

> *Analogía: imagina una vela que se consume sola. Al principio (t=0) queda toda la vela (A0). Con el tiempo va bajando. Nunca llega exactamente a cero, pero se vuelve insignificante.*

---

## Tarea 3 — Método `porcentajeActividad(LocalDateTime fecha)`

Calcula qué **fracción** de la actividad inicial queda en una fecha dada. Devuelve un valor entre 0 y 1.

```
porcentajeActividad(fecha) = actividad(fecha) / getActividadInicial()
```

> Si la actividad inicial era 1.000.000 Bq y ahora es 100.000 Bq, devuelve **0.1** (queda el 10%).

---

## Tarea 4 — Método `getFechaSegura()`

Devuelve la fecha exacta en que la actividad baja al **10% de la actividad inicial** (porcentajeActividad = 0.1). Usa `Utilidades.biseccion` para encontrarla.

### Paso 1 — Calcular `tMax` con un bucle

`biseccion` necesita un límite superior. Como no sabemos cuánto tiempo tardará, lo calculamos:

- Empieza con `tMax` igual a la semivida del isótopo.
- En cada iteración, duplica `tMax`.
- Para cuando `porcentajeActividad(fechaEntrega.plusSeconds(tMax)) < 0.1`.

### Paso 2 — Llamar a `biseccion`

```java
double tSeg = Utilidades.biseccion(
    t -> porcentajeActividad(fechaEntrega.plusSeconds((long) t)),
    0.1,   // valor objetivo
    0,     // límite inferior
    tMax   // límite superior
);
```

> **Importante:** la lambda debe devolver el porcentaje (0..1) y el valor objetivo debe ser `0.1`. No mezcles porcentajes con actividades en Bq.

### Paso 3 — Convertir a fecha

```java
fechaSegura = this.fechaEntrega.plusSeconds((long) tSeg);
```

---

## Tarea 5 — Coste de refrigeración

### 5a) Potencia irradiada

```
P_irr(t) = actividad(fechaEntrega.plusSeconds(t)) × E_desintegracion
```

`E_desintegracion` se obtiene de `Utilidades.energias.get(this.isotopo)`. El resultado está en vatios (W).

### 5b) Potencia eléctrica de refrigeración

```
P_ele(t) = P_irr(t) / 4
```

Define `P_ele` como una lambda para pasársela a `Utilidades.integrar`.

### 5c) Energía total gastada

```
E_gastada = integrar(P_ele, 0, tSeg)   [en Julios]
```

> *Analogía: igual que el recibo de la luz acumula el consumo de todo el mes, aquí acumulamos la potencia eléctrica durante todo el tiempo de refrigeración.*

### 5d) Coste en euros

```
coste = E_gastada / 3.600.000   (1 kWh = 3.600.000 J, precio 1 €/kWh)
```

---

## Tarea 6 — Método `toFactura()`

Devuelve un `String` con el bloque de texto de la factura de ese residuo (el formato se mostró en la sección de ficheros). Dentro del método llama a `getFechaSegura()` y a `getCosteRefrigeracion()`.

Puedes usar `String.format()` o `StringBuilder`. Para formatear fechas puedes usar `DateTimeFormatter`.

> Incluye la línea de guiones al final (`"----------------------------------------"`) para que los bloques queden visualmente separados en el fichero.

---

## Tarea 7 — Método `main`

Lee `residuos.csv` y genera `factura.txt` siguiendo estos pasos:

1. Abrir el CSV con `BufferedReader` + `FileReader`.
2. Leer y descartar la primera línea (cabecera).
3. Para cada línea, separar campos con `linea.split(";")`.
4. Crear un objeto `Radionuclido` con los datos de la línea.
5. Llamar a `toFactura()` para obtener el bloque de texto.
6. Escribir los bloques en `factura.txt` con `BufferedWriter` + `FileWriter`.

> Gestiona las excepciones de E/S con `try-catch` o declarando `throws IOException`.

---

## Resumen — orden de implementación

| # | Método / Clase                              | Depende de          |
|---|---------------------------------------------|---------------------|
| 1 | `Radionuclido` + `getActividadInicial()`    | —                   |
| 2 | `actividad(LocalDateTime)`                  | Tarea 1             |
| 3 | `porcentajeActividad(LocalDateTime)`        | Tarea 2             |
| 4 | `getFechaSegura()`                          | Tarea 3 + biseccion |
| 5 | `getCosteRefrigeracion()`                   | Tareas 2 y 4        |
| 6 | `toFactura()`                               | Tareas 4 y 5        |
| 7 | `main()`                                    | Todas               |

---

*Criterios de evaluación: corrección del código, uso adecuado de la POO (encapsulación, métodos bien definidos), manejo de excepciones en E/S, y legibilidad (nombres descriptivos, estructura). Las clases `Utilidades` y `FuncionUnivariable` ya están implementadas y no deben modificarse.*

---

## Chuleta — `java.time`

### Crear un `LocalDateTime`

```java
// Fecha y hora concreta
LocalDateTime fecha = LocalDateTime.of(2025, 4, 14, 9, 0);  // 2025-04-14T09:00

// Fecha y hora actuales
LocalDateTime ahora = LocalDateTime.now();
```

### Sumar tiempo a un `LocalDateTime`

```java
LocalDateTime masTarde = fecha.plusSeconds(3600);   // + 1 hora
LocalDateTime masTarde = fecha.plusDays(30);        // + 30 días
LocalDateTime masTarde = fecha.plusYears(10);       // + 10 años
```

> En este examen usarás casi siempre `plusSeconds(long t)`, porque las fórmulas trabajan en segundos.

### Calcular segundos entre dos fechas

```java
// Devuelve un long: los segundos transcurridos de 'inicio' a 'fin'
long t = ChronoUnit.SECONDS.between(inicio, fin);
```

> Esto es exactamente lo que necesitas para calcular `t` en la fórmula exponencial.
> Si `fin` es anterior a `inicio`, el resultado es negativo — asegúrate de pasarlos en orden correcto.

### Comparar fechas

```java
fecha.isAfter(otraFecha)    // ¿fecha es posterior?
fecha.isBefore(otraFecha)   // ¿fecha es anterior?
fecha.isEqual(otraFecha)    // ¿son iguales?
```

### Extraer campos de un `LocalDateTime`

```java
int anyo  = fecha.getYear();        // 2025
int mes   = fecha.getMonthValue();  // 4
int dia   = fecha.getDayOfMonth();  // 14
int hora  = fecha.getHour();        // 9
int min   = fecha.getMinute();      // 0
```

### Formatear un `LocalDateTime` como texto

```java
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
String texto = fecha.format(fmt);   // "2025-04-14T09:00"
```

### Resumen de conversiones de tiempo

| Unidad   | En segundos          |
|----------|----------------------|
| 1 minuto | 60 s                 |
| 1 hora   | 3 600 s              |
| 1 día    | 86 400 s             |
| 1 año    | ≈ 31 557 600 s       |
