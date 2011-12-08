require 'rubygems'
require 'mechanize'

client = WWW::Mechanize.new
ARGV.each do |name|
    STDERR.puts name
    file = File.open name, "rb"
    html = client.html_parser.parse file.read, nil, "WINDOWS-1251"
    data_rows = html.xpath "/html/body/table[last()]/tr[last()]/td/table[last()]/tr/td[2]/div/table/tr[not(td[2]/@colspan)]"
    columns = data_rows[0].xpath("count(td)") + 1
    (1..columns).each do |i|
       numbers = []
       data_rows.each do |row|
           numbers.push row.xpath("td[" + i.to_s + "]/nobr").text.strip
       end
       puts numbers.join "\t"
    end
    file.close
end



