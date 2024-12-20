# 使用 llvm-link 将两个文件链接，生成新的 IR 文件
llvm-link ./llvm-test/my.ll ./llvm-test/lib.ll -S -o ./llvm-test/out.ll

# 用 lli 解释运行
lli ./llvm-test/out.ll