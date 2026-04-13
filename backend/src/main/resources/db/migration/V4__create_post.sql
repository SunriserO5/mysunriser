DELETE from post WHERE slug LIKE 'testblog%';
INSERT INTO post (slug, title, summary, content, status, published_at)
VALUES ('helloworld', 'Hello World', '创建这个小站的初心',
        '>我不同意你的观点，但我誓死捍卫你说话的权利 ——伏尔泰

### Hello World
第一次听到这句话，是在中学的语文课堂上。彼时尚为年幼，只把它当作枯燥无味的外国名言来看。但当有所成长，回想之中便发现这来自启蒙旗手的名言早已刻在少年的记忆中。

于是乎，在创立这个个人小天地之时，我就把它当成想要恪守的标准，用当今的话来说：“行动指南”

至于想要表达什么记录什么，我只能坦诚的回答：我也不知道。但我所知道的是，这个世界上总要有些批评的声音；我知道过去的辉煌年代与经历的苦难时期；我知道前方有未尽篇章，等待我们去书写。就像我在给朋友的信中写的那样：“为什么我总是在批评？因为我对她爱的深沉。”

当民粹主义被冠冕堂皇的拥上舞台，异议之音淹没在狂妄的民族主义海洋之中。有人叹息，有人离去，有人奔走呼号。我只愿心中有净土，**“曳尾于涂中。”**

```python
def greet(name):
    print(f"Hello, {name}")

greet("MySunriser")
```', 'Published', NOW());