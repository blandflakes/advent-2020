# Day 1

## Part 1

This could be done by loading everything into memory and doing some nested loops. Seems like it would be faster, though, to do it in one pass - shove everything into a Set. As we go down the line, we know what the other number is, because we subtract it from 2020. So we can check as we read, and if we find the other number in the set, we're done. This isn't necessarily an asymptotic improvement, since in the worst case we still put every element in memory.

## Part 2.

Well damnit. I don't think the Set trick actually works for three numbers - I think the solution basically requires me to nest at least one loop, and requires me to have the numbers in-memory (or to iterate the file several times). So much for elegance. Maybe I can get a very readable for-comprehension on this, once it's all in memory, though. This would actually be a nice time to whip out a kanren implementation.

# Day 2

## Part 1

Lot of string mangling here. Think I'm just going to do the naive "parse everything out" thing and see how it goes. Ok, easy, if lacking in validation.

## Part 2

This seems like I just have to add another validation function...

# Day 3

## Part 1

Ug. I HATE traversal questions. And this one looks like it theoretically wants infinite sequences to the right. Scala has some support for that, though it's not as nice as Clojure. Could
also just calculate how many I'm going to need based on the length of the file. Not sure it's going to be straightforward to do the whole problem within the buffered file... might just be cleaner to load into memory and build nested lazy lists from there.

Couldn't find a way to do any cute reduce, and my parsing code is hilariously inefficient. but it's solved.

## Part 2

Oh man I love when these just generalize (aside from having to promote from Int to Long).

# Day 4

## Part 1

Well, it's not traversal, but there's more janky parsing. Oh well. Let's just rip this stuff into a map and do the dumbest possible validation. Don't want to get too fancy until part 2.

## Part 2

Ug. per-field validation. Not surprising. I could go find a library, or I could just... regex the whole thing? Probably both regex and some validation (don't want to regex year ranges...).

Seems like they could have done more with optional fields - I just ended up not even looking at cid.

# Day 5

## Part 1

Seems like a straightforward binary search, but of course it's really easy to be off by one in many cases. Yup, spent a lot of time to figure out that I needed to step up by 1 when picking the higher range.

## Part 2

This seems... under-specified. I guess the seats probably present in sorted order? So the algorithm looks to be "find the spot in the list where a number is missing, but the surrounding
numbers are there." In other words, look to the spot where the current seat is 2 ids below the next seat?