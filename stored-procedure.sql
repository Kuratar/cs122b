delimiter $$
create procedure add_movie(in t varchar(100), y int, d varchar(100), s_name varchar(100), g_name varchar(32),
                           out movieExists int, out starExists varchar(10), out genreExists int)
begin
	if ((t,y,d) in (select title,year,director from movies where title=t and year=y and director=d)) then
        select 1 into movieExists;
        select "-1" into starExists;
        select -1 into genreExists;
    else
        select 0 into movieExists;
        if (exists(select id from stars where name=s_name)) then
            select id from stars where name=s_name into starExists;
        else
            select "-1" into starExists;
        end if;
        if (exists(select id,name from genres where name=g_name)) then
            select id from genres where name=g_name into genreExists;
        else
            select -1 into genreExists;
        end if;
    end if;
end $$
delimiter ;