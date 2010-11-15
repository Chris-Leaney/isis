package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import net.sf.isiscontrib.bdd.fitnesse.internal.AbstractFixture;
import net.sf.isiscontrib.bdd.fitnesse.internal.CellBindingForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.util.FitnesseUtil;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.Constants;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.fixtures.AliasItemsInListPeer;

import fit.Parse;

public class AliasItemsInListForFitNesse extends AbstractFixture<AliasItemsInListPeer> {

    public AliasItemsInListForFitNesse(final AliasRegistry aliasesRegistry, final String listAlias) {
        this(aliasesRegistry, listAlias, CellBindingForFitNesse.builder(Constants.TITLE_NAME, Constants.TITLE_HEAD)
            .build(), CellBindingForFitNesse.builder(Constants.TYPE_NAME, Constants.TYPE_HEAD).optional().build(),
            CellBindingForFitNesse.builder(Constants.ALIAS_RESULT_NAME, Constants.ALIAS_RESULT_HEAD_SET).autoCreate()
                .build());
    }

    private AliasItemsInListForFitNesse(final AliasRegistry aliasRegistry, final String listAlias,
        final CellBinding titleBinding, final CellBinding typeBinding, final CellBinding aliasBinding) {
        super(new AliasItemsInListPeer(aliasRegistry, listAlias, titleBinding, typeBinding, aliasBinding));
    }

    @Override
    public void doTable(final Parse table) {
        final Parse listAliasCell = table.parts.parts.more;

        try {
            getPeer().assertIsList();
        } catch (StoryValueException e) {
            FitnesseUtil.exception(this, listAliasCell, e.getMessage());
        }

        super.doTable(table);
    }

    @Override
    public void doRow(final Parse row) {

        if (!getPeer().isList()) {
            return; // skip
        }

        doCells(row.parts);

        try {
            executeRow();
        } catch (final Exception e) {
            reportError(row, e);
        }
    }

    private void executeRow() {
        StoryCell currentCell;
        try {
            currentCell = getPeer().findAndAlias();
            right(FitnesseUtil.asParse(currentCell));
        } catch (StoryBoundValueException ex) {
            FitnesseUtil.exception(this, ex);
        }
    }

}
