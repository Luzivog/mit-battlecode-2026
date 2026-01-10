from jinja2 import Environment, FileSystemLoader

env = Environment(
    loader=FileSystemLoader("."),
    trim_blocks=True,
    lstrip_blocks=True,
)

template = env.get_template("BudgetMap.java.jinja2")

out = template.render()

with open("BudgetMap.java", "w") as f:
    f.write(out)
