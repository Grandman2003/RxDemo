package com.example.rxdemo

import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.rxdemo.databinding.ActivityMainBinding
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.*
import io.reactivex.functions.Function
import io.reactivex.internal.operators.observable.ObservableElementAtMaybe
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    /**
     *  В RxJava есть несколько базовых классов
     *   - [Observable]
     *   - [Flowable]
     *   - [Single]
     *   - [Maybe]
     *   - [Completable]
     *
     *  [Observable]
     *  Используется когда идёт поток данных
     *  методы:
     *  - onNext
     *  - onError
     *  - onComplete
     *
     *  [Flowable]
     *  Используется когда идёт поток данных о важно учитыватть,
     *  что приём данных(приход) может быть быстрее их обработки(добавляют стратегию BackpressureStrategy)
     *  методы:
     *  - onNext
     *  - onError
     *  - onComplete
     *
     *  [Single]
     *  Когда идёт не поток данных, а единичная информация
     *  - onError
     *  - onSuccess
     *
     *  [Maybe]
     *  Когда идёт не поток данных, а единичная информация,
     *  но в конце выполнения при любом исходе мы что-то выполняем
     *  - onError
     *  - onSuccess
     *  - onComplete
     *
     *  [Completable]
     *  Когда нам не важен процесс приёма а важен факт завершения или ошибки:
     *  - onError
     *  - onComplete
     **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //postInMainThreadUsingHandler()
        //observableUsageExample()
        //disposableUsage()
        //transformationMaps()
        //otherTransformationOperators()
        //observableFilters()
        //combinationObservables()
        //otherMethodsInObservable()
        observableDos()
    }
    private val disposeBag = CompositeDisposable()

    /** Объект интерфейса [Disposable] позволяет отслеживать состояние подписки на
     * источник с помщью методов:
     * - [Disposable.isDisposed] - который показывает остановлена ли подписка(приём данных)
     * - [Disposable.dispose] - останавливает приём данных(подписку)
     *
     */
    fun disposableUsage(){
        val result: Disposable = Observable.just(1,2,3,4,5)
            .delay(4,TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
               Log.e(TAG,"value is -$it")
            },{

            })

        disposeBag.add(result)
//        Handler().postDelayed({
//            Log.e(TAG,"DISPOSED")
//            result.dispose()
//        },5000)
    }
    /**
     * [CompositeDisposable] ([disposeBag]) - контейнер хранящий добавленные Diposables
     * и применяющий ко всем одни и те же операции(например clear() ызывает метод dispose()
     * у всех Dipsosable внутри
     */
    override fun onDestroy() {
        disposeBag.clear()
        super.onDestroy()
    }

    /**
     * В RxJava есть методы трансформации поступпающих из источника данных
     * Большим классом таких методов являются Map-ы:
     * - [Observable.map] - применяет некоторое изменение к данным из источника(как у Iterable, Sequence или Array)
     * - [Observable.flatMap] - для каждого элемента источника данных применяет некоторую функцию(как map)
     * и создаёт новый {[Observable],[Single],[Сompletable],[Maybe],[Flowable]} соответствующий исходному типу,
     * причём порядок исходящих данных может отличаться от порядка
     * входящих данных(кто раньше обработался, тот и раньше вышел(несинхронизированно))
     * - [Observable.switchMap] - для каждого элемента источника данных применяет некоторую функцию(как map)
     * и создаёт новый {[Observable],[Single],[Сompletable],[Maybe],[Flowable]} соответствующий исходному типу,
     * причём если элементы приходят одновременно, то он берёт последний из них и обрабатывает тольео его
     * в не зависимости от задержки, которая произойдёт в процессе обработки(если элементы приходят последовательно,
     * то он их все обработает)
     * - [Observable.concatMap] - для каждого элемента источника данных применяет некоторую функцию(как map)
     * и создаёт новый {[Observable],[Single],[Сompletable],[Maybe],[Flowable]} соответствующий исходному типу,
     * причём порядок исходящих данных СООТВЕТСТВУЕТ!! порядку
     * входящих данных(первый пришёл - первый вышел(синхронизированно))
     *
     */
    fun transformationMaps(){
        val resultMap: Disposable = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений")
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { if("и" in it.lowercase())it + " Нефрология" else it + " Кардиология"}
            .subscribe({
                Log.e(TAG,"It is $it")
            },{

            })

        val resultFlatMap: Disposable = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений")
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap{
                val Tdelay = Random().nextInt(10)
                Log.e(TAG,"delay Flat is $Tdelay")
                Observable.just(it).delay(Tdelay.toLong(),TimeUnit.SECONDS)
            }
            .subscribe({
                Log.e(TAG,"Flat is $it")
            },{

            })

        val resultSwitchMap: Disposable = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений")
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .switchMap{
                val Tdelay = Random().nextInt(10)
                Log.e(TAG,"delay Switch is $Tdelay")
                Observable.just(it).delay(Tdelay.toLong(),TimeUnit.SECONDS)
            }
            .subscribe({
                Log.e(TAG,"Switch is $it")
            },{

            })

        val resultConcatMap: Disposable = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений")
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .concatMap{
                val Tdelay = Random().nextInt(10)
                Log.e(TAG,"delay Concat is $Tdelay")
                Observable.just(it).delay(Tdelay.toLong(),TimeUnit.SECONDS)
            }
            .subscribe({
                Log.e(TAG,"Concat is $it")
            },{

            })
    }

    /** Кроме операторов трансформации Map есть и другие, основные из них
     *  - [Observable.buffer] - разбивает входящие данные на Bundleы(списки)
     *  с указанным в методе размером, если элементов меньше, чем указанный размер, то создаёт
     *  Bundle из оставшихся элеметов.
     *  - [Observable.groupBy] -  аналогичен одноимённому оператору [Collection.groupBy].
     *  Выполняем группировку объектов на списки по заданному критерию и возвращает объекты
     *  [GroupedObservable], где значению ключа соответствует [Observable] с сгруппированными элементами
     *  - [Observable.scan] - аналогичен методу из библиотеки [Collection] - [Collection.reduce].
     *  Над следующим элементом применяется некоторая операция с учётом предыдущего результата
     *
     *
     */
    fun otherTransformationOperators(){
        val resultBuffer: Disposable = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений")
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .buffer(2)
            .subscribe({
                Log.e(TAG,"There ${it.size} items")
                it.forEach {item ->
                    Log.e(TAG,"Buffer item is $item")
                }

            },{

            })

        val resultGroupBy: Disposable = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений")
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .groupBy(object: Function<String,Boolean>{
                override fun apply(t: String): Boolean {
                    return t.lowercase().contains("а")
                }
            }).subscribe({
                Log.e(TAG,"${it.key}")
                if(it.key==true) {
                    val result = it
                        .subscribeOn(Schedulers.newThread())
                        .subscribe({name ->
                            Log.e(TAG, "Item name is ${name}")},{
                        }
                    )
                    Log.e(TAG, "Grouped item are ${it}")
                }},{

            }
            )

        val resultScan: Disposable = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .scan(object:BiFunction<String,String,String>{
                override fun apply(t1: String, t2: String): String {
                    return "$t1 $t2"
                }

            })
            .subscribe({result->
                Log.e(TAG,"Scan tesult is $result")
            },{

            })

        val resultScanFactorials = Observable.just(1,2,3,4,5,6)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .scan(object: BiFunction<Int,Int,Int>{
                override fun apply(t1: Int, t2: Int): Int {
                    return t1*t2
                }

            }).subscribe {
                Log.e(TAG,"Factorial is $it ")
            }
    }

    /**
     * Операторы фильтрациии:
     * - [Observable.debounce] - ждёт указанное количество времени перед тем как пропустить
     * последний пришедший результат далее, если приходит новый элемент, то обновляется таймер
     * - [Observable.distinct] - фильтрует поток данных по принципу [Set]:
     * убиарает дубликаты
     * - [Observable.elementAt] - возвращает [Maybe] с элементом из источника
     * данных [Observable],имеющим указанный в аргументе метода индекс.
     * - [Observable.filter] - фильтрует данные по условию из предикаты
     * - [Observable.ignoreElements] = игнорирует весь поток данных, главное
     * факт завершения[onComplete] (Как [Completable] только для потока данных)
     * - [Observable.sample] - с некоторой периодичностью возвращает последний
     * предоставленный элемент из предоставляемого источника данных
     * - [Observable.skip] - пропускает заданное в методе кол-во первых полученных
     * элемнтов
     * - [Observable.skipLast] - пропускает заданное в методе кол-во последних полученных
     * элемнтов
     * - [Observable.take] - берёт заданное в методе кол-во первых полученных
     * элемнтов
     * - [Observable.takeLast] - бёрёт заданное в методе кол-во последних полученных
     * элемнтов
     */
    fun observableFilters(){
        val searchDebounce = RxTextView.textChanges(binding.searchText)
            .debounce(500,TimeUnit.MILLISECONDS)
            .subscribe(
            {
                Log.e(TAG,"Search string is $it")
            },{

            }
        )

        val disposeDebounce = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений")
            .debounce(1,TimeUnit.SECONDS)
            .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
            .subscribe({
                Log.e(TAG,"Debounce item is $it")
            },{

            })

        val disposableDistinct = Observable.just("Мамка","Алексей",
            "Владимир","Мамка","Георгий","Дмитрий","Евгений")
            .distinct()
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e(TAG,"Distinct item is $it")
            },{},{})

        val priceArray= arrayOf(
            Price(100,"rub","Dollar"),
            Price(100,"rub","Dollar"),
            Price(100,"rub","Dollar"),
            Price(100,"rub","Dollar"),
            Price(150,"rub","Dollar"),
            Price(200,"sum","Sola"),
            Price(100,"rub","Dollar"))

        val distinctCalssDisposable = Observable.fromArray(*priceArray)
            .distinct()
            .subscribe{
                Log.e(TAG,"DistinctPrice is ${it.cost}")
            }

        val disposableFilter = Observable.just("Мамка","Алексей",
        "Владимир","Мамка","Георгий","Дмитрий","Дмитрий","Евгений")
            .filter(object: Predicate<String>{
                override fun test(t: String): Boolean {
                   return t.lowercase().contains("и")
                }

            })
            .distinct()
            .filter { it.length<8 }
            .filter { it.lowercase().contains("д") }
            .map { "$it Владимирович" }
            .subscribe({
                Log.e(TAG,"Filter name is ${it}")
            },{})

        val elementsList= listOf<String>("Мамка","Алексей",
            "Владимир","Мамка","Георгий","Дмитрий","Дмитрий","Евгений")

        val disposabelElement = Observable.fromIterable(elementsList)
            .elementAt(4)
            .subscribe({
                Log.e(TAG,"ElementAt 4 is ${it}")
            },{})

        val disposableIgnore = Observable.fromIterable(elementsList)
            .ignoreElements()
            .doOnComplete {  }
            .doOnError { }

        val timedObservable = Observable.just(1,2,3,4,5,6,7)
            .zipWith(Observable.interval(0,1,TimeUnit.SECONDS),object: BiFunction<Int,Any,Int>{
                override fun apply(t1: Int, t2: Any): Int {
                    return t1
                }
            })
            .sample(2,TimeUnit.SECONDS)
            .subscribe {
                Log.e(TAG,"Sample emitted element is ${it}")
            }

        val disposableSkip= Observable.just("Мамка","Алексей",
            "Владимир","Мамка","Георгий","Дмитрий","Евгений")
            .distinct()
            .skip(3)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e(TAG,"Skip item is $it")
            },{},{})

        val disposableSkipLast= Observable.just("Мамка","Алексей",
            "Владимир","Мамка","Георгий","Дмитрий","Евгений")
            .distinct()
            .skipLast(3)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e(TAG,"SkipLast item is $it")
            },{},{})

        val disposableTake= Observable.just("Мамка","Алексей",
            "Владимир","Мамка","Георгий","Дмитрий","Евгений")
            .distinct()
            .take(3)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e(TAG,"Take item is $it")
            },{},{})

        val disposableTakeLast= Observable.just("Мамка","Алексей",
            "Владимир","Мамка","Георгий","Дмитрий","Евгений")
            .distinct()
            .takeLast(3)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.e(TAG,"TakeLast item is $it")
            },{},{})
    }

    data class Price(val cost: Int,val currency: String, val title: String)

    /**Observable Combinations методы, позволяют определённым образом
     *  комбинировать приходящую из разных источников информацию:
     *  - [Observable.zipWith] - попарно проводит некоторую операцию с элементами из Observable,
     *  операцию с первым элементом первого источника проводит с первым элементом второго источника,
     *  со вторым элементом первого источника идёт второй второго и т.д.
     *  - [Observable.mergeWith] - сливает 2 потока в один причём порядок прихода данных не важен
     *  (кто первый пришёл тот первый и вышел, поочерёдно)
     *  - [Observable.join] - похож на [Observable.combineLatest], с важной особенностью, что мы
     *  указываем суолько дополнительно времени элемент может использоваться в скрещивании,то есть
     *  идёт комбинации не с последним элементом, а с ныненживущим. Причём этот период жизни указывается для
     *  всех потоков данных и может отличаться.
     *  - [Observable.combineLatest] - комбинирует последние пришедшие результаты, пример:
     *  если 2 аотока имебт задержку 300мс , а третий 100мс, то каждые 100мс будут скомбинированы
     *  старые значения 2 первых потоков и новое 3-его, а каждые 300мс будут комбминироваться объекты из всех
     *  новых значений
     *  - [Observable.concatWith] - функция сперва пропускает данные с одного источника,
     *  а когда они закончились начинает пропускать данные с другого заданного в аргументе метода источника
     *  - [Observable.switchOnNext] - работает когда один поток данных подключается к другому с помощью
     *  какой-либо комбинации, функция принимкет результат комбинации нескольких потоков и когда внутри комбинации
     *  вызывается элемент следующего потока,то метод полностью переключаетсяа него
     */
    fun combinationObservables(){
        var names = Observable.just("Алексей",
         "Владимир","Георгий","Дмитрий","Евгений","Непопал")
        var surnames = Observable.just("Иванов","Петров","Сидоров","Громов","Филинов")
        val zipWithObservable = names.zipWith(surnames, object: BiFunction<String,String,String>{
            override fun apply(t1: String, t2: String): String {
                return "$t1 $t2"
            }

        }).subscribeOn(Schedulers.from(Executors.newFixedThreadPool(2)))
            .subscribe {
                Log.e(TAG,"Zipped name is $it")
            }
        disposeBag.add(zipWithObservable)
        val goods = Observable.just("Toaster","Owen","Fan")
        val prices = Observable.just("199usd","ErrorUsd","255usd")

        disposeBag.add(goods.zipWith(prices,object: BiFunction<String,String,String>{
            override fun apply(t1: String, t2: String): String {
                return if (t1=="Owen") "Oh maaan, damn it" else "$t1 - price: $t2"
            }
        }).subscribe{
            Log.e(TAG,"ZipGoods: $it")
        })

        disposeBag.add(names.zipWith(Observable.interval(300,TimeUnit.MILLISECONDS),object: BiFunction<String,Long,String>{
            override fun apply(t1: String, t2: Long): String {
                return t1
            }
        }).mergeWith(surnames.zipWith(Observable.interval(500,TimeUnit.MILLISECONDS),object: BiFunction<String,Long,String>{
            override fun apply(t1: String, t2: Long): String {
                return t1
            }
        })).subscribe ({
            Log.e(TAG,"Merge resilt is $it")
        },{
            it.printStackTrace()
        }))

        val temperatureFactoryFirst  = Observable.fromArray(11,90,102,100,88)
        val temperatureFactorySecond  = Observable.fromArray(-5,-10,-58,-80,-90)
        disposeBag.add(Observable.combineLatest(temperatureFactoryFirst.zipWith(Observable.interval(
            300,TimeUnit.MILLISECONDS),object: BiFunction<Int,Long,Int>{
                override fun apply(t1: Int, t2: Long): Int {
                    return t1
                }
            }), temperatureFactorySecond.zipWith(Observable.interval(500,TimeUnit.MILLISECONDS),
            object:BiFunction<Int,Any,Int>{
                override fun apply(t1: Int, t2: Any): Int {
                    return t1
                }
            }), object : BiFunction<Int,Int,Factories>{
            override fun apply(t1: Int, t2: Int): Factories {
                MainActivity.Factories.firstFactory = t1
                MainActivity.Factories.secondFactory =t2
                return MainActivity.Factories
            }
        }).subscribe{
            Log.e(TAG, "First factory temp is ${it.firstFactory}, second is ${it.secondFactory}")
        })

        disposeBag.add(names.zipWith(Observable.interval(300,TimeUnit.MILLISECONDS),
            object: BiFunction<String,Any,String>{
                override fun apply(t1: String, t2: Any): String {
                    return t1
                }
            }).concatWith(surnames.zipWith(Observable.interval(500,TimeUnit.MILLISECONDS),
            object: BiFunction<String,Any,String>{
                override fun apply(t1: String, t2: Any): String {
                    return t1
                }
            } )).subscribe{
                Log.e(TAG,"Concat result is $it")
        })

        names = names.zipWith(Observable.interval(300,TimeUnit.MILLISECONDS),
            object: BiFunction<String,Any,String>{
                override fun apply(t1: String, t2: Any): String {
                    return t1
                }

            })
        surnames = Observable.zip(surnames,Observable.interval(500,TimeUnit.MILLISECONDS),
        object: BiFunction<String, Any,String>{
            override fun apply(t1: String, t2: Any): String {
                return t1
            }

        })
        /**
         * [Observable.timer] - выпускает один элемент через промежуток времени
         * [Observable.interval] - через каждый промежуток времени выпускает по элементу
        */
       disposeBag.add(Observable.switchOnNext(Observable.timer(150L,TimeUnit.MILLISECONDS).map { it -> names }
           .concatWith(Observable.timer(300L,TimeUnit.MILLISECONDS).map { it ->surnames })).subscribe {
            Log.e(TAG, "Switch result -> $it")})

        val right = Observable.interval(300,TimeUnit.MILLISECONDS)
        val left = Observable.interval(100,TimeUnit.MILLISECONDS)

        disposeBag.add(left.join(right,object: Function<Long,Observable<Long>>{
            override fun apply(t: Long): Observable<Long> {
                return Observable.timer(300,TimeUnit.MILLISECONDS)
            }

        },object : Function<Long, Observable<Long>>{
            override fun apply(t: Long): Observable<Long> {
                return Observable.timer(100,TimeUnit.MILLISECONDS)
            }

        },object: BiFunction<Long,Long,Long>{
            override fun apply(t1: Long, t2: Long): Long {
                Log.e(TAG,"Left $t1, right $t2")
                return t1+t2
            }

        }).take(10)
            .subscribe({
                       Log.e(TAG,"Join result is $it")
            },{
                    Log.e(TAG,"ErrorJoin result is ${it.localizedMessage}")
            }))

 }

    /** В xJava помимио методов фильтрации, трансформации и комбинации существуют и другие полезные
     * методы, такие как:
     * -  [Observable.all] - как и в StreamAPI Java или [Collection.all] возвращает
     * [Boolean] в зависимости от выполнения условия(предикаты) для всех элементов, причёи так как нам нужен
     * класс типа [Observable], но который содержит в себе только один [Boolean], то рассматриваемый метод
     * возвращает [Single<Boolean>]
     * -  [Observable.contains] - как и в StreamAPI Java или [Collection.contains] возвращает
     * [Boolean] в зависимости от выполнения условия(предикаты) хотя бы для одного элемента, причёи так как нам нужен
     * класс типа [Observable], но который содержит в себе только один [Boolean], то рассматриваемый метод
     * возвращает [Single<Boolean>]
     * - [Observable.delay] - перед тем как передать поток данных дальше на обработку задерживает весь его
     * на указанное в методе время
     * - [Observable.count] - возвращает кол-во элеметов в потоке данных в объекте [Single<Long>]
     * - [Observable.defaultIfEmpty] - возвращает заданное в методе значение если источник данных пуст,
     * возвращает объект [Observable<T>], где [T] - тип указанного
     * по умолчанию значения(данный Observable будет иметь один элемент)
     * - [Observable.timeInterval] - к каждому элементу добавляет значение времени, которое прошло
     * от прихода предыдущего элемента
     * - [Observable.timestamp] - к каждому элементу добавляет точное значение времени, когда элемент был
     * получен в формате Unix-время.
     * - [Observable.timeout] - выкидывает ошибку если за указынное в методе время не появилось ни одного
     * элемента
     */
    fun otherMethodsInObservable(){
        val names = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений","Непопал")
        disposeBag.add(names.all(object: Predicate<String>{
            override fun test(t: String): Boolean {
                return t.contains("И")
            }
        }).subscribe({
            Log.e(TAG,"All result is $it")
        },{

        }))

        disposeBag.add(Observable.range(1,8).all(Predicate{
            (it/1)==it
        }).subscribe({
            Log.e(TAG,"All result del is $it")
        },{

        }))

        disposeBag.add(names.delay(5,TimeUnit.SECONDS).subscribe {
            Log.e(TAG,"Delay result  is $it")
        })

        disposeBag.add(names.count().subscribe({Log.e(TAG,"Names count is $it")},{}))
        disposeBag.add(names.skip(6).defaultIfEmpty("Names are eeeempty").subscribe({next ->
            Log.e(TAG,next)
        },{error -> },{}))
        disposeBag.add(names.zipWith(Observable.interval(150,TimeUnit.MILLISECONDS),BiFunction{
            t1,t2 -> t1
        }).timeInterval().subscribe {
            Log.e(TAG,"Time spent ${it.time()}, value is ${it.value()}")
        })
        disposeBag.add(names.zipWith(Observable.interval(150,TimeUnit.MILLISECONDS),BiFunction{
                t1,t2 -> t1
        }).timestamp().subscribe {
            Log.e(TAG,"Was got at ${it.time()}, value is ${it.value()}")
        })
        disposeBag.add(names.zipWith(Observable.interval(500,TimeUnit.MILLISECONDS),BiFunction{
                t1,t2 -> t1
        }).timeout(300,TimeUnit.MILLISECONDS).subscribe({
            Log.e(TAG,"Timeout result  is $it")
        },{
            error -> Log.e(TAG, error.localizedMessage)

        }))
    }

    object Factories
    {
        var firstFactory: Int = 0
        var secondFactory: Int = 0
    }

    /** Семейство операторов Do:
     * - [Observable.doOnNext] - дублирует метод onNext, нужен как дополненение к бизнеслогике,
     * если после рассматриваемого метода будут проведены ещё операции
     * - [Observable.doOnSubscribe] - - дублирует метод subscribe(содержит), нужен как дополненение к бизнеслогике,
     * если после рассматриваемого метода будут проведены ещё операции
     * - [Observable.doOnError] - дублирует метод onError, нужен как дополненение к бизнеслогике,
     * если после рассматриваемого метода будут проведены ещё операции
     * - [Observable.doOnComplete] - дублирует метод onComplete, нужен как дополненение к бизнеслогике,
     * если после рассматриваемого метода будут проведены ещё операции
     * - [Observable.doOnDispose] - используется, если перед остановкой Observable нужно проделать некоторые действия
     */
    fun observableDos(){
        val names = Observable.just("Алексей",
            "Владимир","Георгий","Дмитрий","Евгений","Непопал")
        disposeBag.add(names
                        .doOnNext(object: Consumer<String>{
                            override fun accept(t: String?) {
                                Log.e(TAG,"do on next $t")
                            }

                        })
                        .doOnSubscribe{
                            Log.e(TAG,"do on subscribe")
                        }
                        .doOnError{
                            Log.e(TAG,"do on error ${it.localizedMessage}")
                        }
                        .doOnComplete(object: Action{
                            override fun run(){
                                Log.e(TAG,"do on complete")
                            }
                        })
                        .filter { it.lowercase().contains("и")  }.subscribe{
                    Log.e(TAG,"Does result is $it")
            })
    }
    fun observableUsageExample(){
        binding.buttonTest.setOnClickListener {
            Log.e(TAG,"Gachiii clicking")
        }
        val disposable = dataSourceFlowable()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {next ->
                    binding.buttonTest.text = "New int is $next"
                    Log.e(TAG,"New int is $next")
                },
                {error ->
                    Toast.makeText(applicationContext,error.localizedMessage,Toast.LENGTH_LONG).show()
                    Log.e(TAG,error.localizedMessage)
                },{//complete
                    Log.v(TAG,"THATS ALL")
                }
            )

        val disposableSingle = datasourceSingle()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({success->

            },{error->

            })

        val disposeCompletable = datasourceCompletable()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({//complete
                        Log.e(TAG,"Action happened")
            },{error->
                        Log.e(TAG,"Action didn't happen by ${error.localizedMessage}")
            })

        val disposableMaybe = datasourceMaybe()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({success->
                Toast.makeText(applicationContext,"Result success", Toast.LENGTH_LONG).show()
            },{error->
                Toast.makeText(applicationContext,"Result error", Toast.LENGTH_LONG).show()
            },{
                //complete
                Toast.makeText(applicationContext,"Result compltete", Toast.LENGTH_LONG).show()
            })
    }


    fun dataSource(): Observable<Int>{
        return Observable.create {subscriber ->
            for(i in 1..1000){
                Thread.sleep(2000)
                subscriber.onNext(i)
            }
        }
    }

    fun dataSourceFlowable(): Flowable<Int>{
        return Flowable.create({subscriber ->
            for(i in 1..900000){
                subscriber.onNext(i)
            }
            subscriber.onComplete()
        },BackpressureStrategy.LATEST)
    }

    fun datasourceSingle():Single<List<Int>> =
        Single.create {subscriber ->
            val list = listOf<Int>(1,2,3,4,5,6)
            subscriber.onSuccess(list)
        }

    fun datasourceCompletable():Completable =
        Completable.create {subscriber ->
            val list = listOf<Int>(1,2,3,4,5,6)
            subscriber.onComplete()
        }

    fun datasourceMaybe():Maybe<List<Int>> =
        Maybe.create {subscriber ->
            val list = listOf<Int>(1,2,3,4,5,6)
            subscriber.onSuccess(list)
            subscriber.onComplete()
        }

    fun postInMainThreadUsingHandler()=
    thread{
        Thread.sleep(5000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Handler.createAsync(Looper.getMainLooper()).post {
                binding.textView.text = "GOOOOD"
            }
        }
    }

    fun PublishersExamples(){
        //Publishers
        val observable = Observable.just(1,2,3)
        val single  = Single.just(1)
        val flowable = Flowable.just(1,2,3)

        //Disposables(subscribes controllers)
        val dispose: Disposable = observable.subscribe {
            Log.e(TAG,"New data $it")
        }
        val disposableSingle = single.subscribe({success ->
            Log.e(TAG,"New data $success")
        },{err ->
        })
        val disposableFlowable = flowable.subscribe {
            Log.e(TAG,"New data $it")
        }
    }
}
